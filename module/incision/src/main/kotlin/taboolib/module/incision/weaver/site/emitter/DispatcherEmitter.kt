package taboolib.module.incision.weaver.site.emitter

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode
import taboolib.module.incision.annotation.Trim
import taboolib.module.incision.runtime.DispatchSignatureCodec
import taboolib.module.incision.weaver.site.SiteSpec

/**
 * dispatcher 字节码发射工具。
 *
 * 同时提供两种形态：
 *  - 旧：`emit*(mv: MethodVisitor, site)` — 直接向 `MethodVisitor` 发射（streaming 路径 & 旧 recording 路径用）
 *  - 新：`build*(site): InsnList` — 返回 [InsnList]，供 Tree API 重写路径直接 insertBefore/insert 到 [org.objectweb.asm.tree.MethodNode.instructions]
 *
 * 旧方法委托到新方法 + 一次 `list.accept(mv)` 回放以保持行为一致，避免逻辑双维护。
 *
 * 所有 Graft/Bypass/Trim 最终都调 `IncisionBridge.dispatch(sig, self, args)`。
 */
object DispatcherEmitter {

    private const val BRIDGE = "io/izzel/incision/bridge/IncisionBridge"
    private const val DISPATCH_DESC = "(Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"

    /**
     * 生成 dispatch 调用的 targetSig。
     *
     * 格式：`owner.name(desc)return` 或带 adviceId 后缀 `owner.name(desc)return#adviceId`。
     * 后者由 [TheatreDispatcher.dispatch] 按 adviceId 精确路由到单一 AdviceEntry，避免多 advice
     * 共目标（同 chain 内多条 GRAFT/BYPASS/TRIM）时按 phase 匹配误触发全部 entry 的放大问题。
     * 空 adviceId 时退回旧的 phase-only 路径，保留对历史 emission 的兼容。
     */
    fun sigOf(site: SiteSpec): String {
        val base = "${site.target.owner}.${site.target.name}${site.target.descriptor}"
        return DispatchSignatureCodec.compose(base, site.adviceId)
    }

    // ---- Tree API (推荐) ---------------------------------------------------

    /**
     * 发射 dispatcher 调用，栈顶留下 Object 返回值。
     *
     * 产物序列：`LDC sig · self · args[] · INVOKESTATIC dispatch`
     */
    fun buildDispatch(site: SiteSpec): InsnList {
        val list = InsnList()
        list.add(LdcInsnNode(sigOf(site)))
        emitSelfLoad(list, site)
        emitArgsArray(list, site)
        list.add(MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "dispatch", DISPATCH_DESC, false))
        return list
    }

    /** GRAFT：发 dispatcher 并丢弃返回值。 */
    fun buildGraft(site: SiteSpec): InsnList {
        val list = buildDispatch(site)
        list.add(InsnNode(Opcodes.POP))
        return list
    }

    fun buildBypassDispatch(site: SiteSpec): InsnList {
        val list = InsnList()
        list.add(LdcInsnNode(sigOf(site)))
        emitSelfLoad(list, site)
        emitArgsArray(list, site)
        list.add(MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "dispatchBypass", DISPATCH_DESC, false))
        return list
    }

    /**
     * BYPASS：用 dispatcher 调用替代原指令。
     *
     * 流程（[SiteSpec.bypassConsumedDescs] / [SiteSpec.bypassReturnDescriptor] 已由 SiteWeaver 填好时）：
     *  1. dispatch → 栈顶 Object
     *  2. ASTORE tmp
     *  3. 倒序 POP/POP2 弹掉原指令本应消费的栈值
     *  4. ALOAD tmp
     *  5. 按 [SiteSpec.bypassReturnDescriptor] 把 Object 转成期望类型：void → POP；引用 → CHECKCAST；primitive → CHECKCAST + unbox
     *
     * 元数据缺失时退回兼容旧行为：dispatch + （isVoid 时）POP。
     */
    fun buildBypass(site: SiteSpec, isVoid: Boolean): InsnList {
        val tmp = site.bypassTempSlot
        val retDesc = site.bypassReturnDescriptor
        if (tmp < 0 || retDesc.isEmpty()) {
            // 旧兼容路径
            val list = buildDispatch(site)
            if (isVoid) list.add(InsnNode(Opcodes.POP))
            return list
        }
        val list = buildDispatch(site)
        list.add(VarInsnNode(Opcodes.ASTORE, tmp))
        emitPopConsumed(list, site.bypassConsumedDescs)
        when {
            retDesc == "V" -> { /* 不再 ALOAD，原指令本来就不产值 */ }
            retDesc.startsWith("L") || retDesc.startsWith("[") -> {
                list.add(VarInsnNode(Opcodes.ALOAD, tmp))
                val castTarget = when {
                    retDesc.startsWith("L") && retDesc.endsWith(";") -> retDesc.substring(1, retDesc.length - 1)
                    else -> retDesc
                }
                if (castTarget != "java/lang/Object") {
                    list.add(TypeInsnNode(Opcodes.CHECKCAST, castTarget))
                }
            }
            else -> {
                list.add(VarInsnNode(Opcodes.ALOAD, tmp))
                addUnbox(list, retDesc)
            }
        }
        return list
    }

    /** 按 [descs]（栈底→栈顶顺序拼接的 JVM 描述符）从栈顶到栈底依次 POP/POP2。 */
    private fun emitPopConsumed(list: InsnList, descs: String) {
        if (descs.isEmpty()) return
        val parsed = parseConcatenatedDescs(descs)
        // 倒序：先弹栈顶
        for (i in parsed.indices.reversed()) {
            val d = parsed[i]
            if (d == "J" || d == "D") list.add(InsnNode(Opcodes.POP2))
            else list.add(InsnNode(Opcodes.POP))
        }
    }

    /** 拆分诸如 `Lowner;ID[Ljava/lang/String;` 这样的拼接描述符串。 */
    private fun parseConcatenatedDescs(s: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        while (i < s.length) {
            val start = i
            while (i < s.length && s[i] == '[') i++
            when (s.getOrNull(i)) {
                'L' -> {
                    val semi = s.indexOf(';', i)
                    if (semi < 0) return out
                    out += s.substring(start, semi + 1); i = semi + 1
                }
                null -> return out
                else -> { out += s.substring(start, i + 1); i++ }
            }
        }
        return out
    }

    /**
     * TRIM 调度入口 —— 按 [SiteSpec.trimKind] 分发：
     *  - RETURN（默认）：栈前 [origVal]；调 dispatcher 后非 null 替换；栈后 [origVal | dispatchRet]
     *  - ARG / VAR：与栈无关，从 LV slot 读 → 调 dispatcher → 非 null CHECKCAST + 写回 slot
     */
    fun buildTrim(site: SiteSpec): InsnList = when (site.trimKind) {
        Trim.Kind.ARG, Trim.Kind.VAR -> buildTrimSlot(site)
        Trim.Kind.RETURN, null -> buildTrimReturn(site)
    }

    private fun buildTrimReturn(site: SiteSpec): InsnList {
        val desc = site.trimReturnDescriptor
        return when {
            desc.isEmpty() || desc.startsWith("L") || desc.startsWith("[") -> buildTrimReturnReference(desc)
            desc == "V" -> InsnList()
            desc == "J" || desc == "D" -> buildTrimReturnWide(desc)
            else -> buildTrimReturnNarrow(desc)
        }.let { tail ->
            val out = buildDispatch(site)
            out.add(tail)
            out
        }
    }

    /** 引用类型 / 未知：栈前 [orig:R]，栈后 [orig|ret-as-R]。 */
    private fun buildTrimReturnReference(desc: String): InsnList {
        val list = InsnList()
        val keep = LabelNode()
        val end = LabelNode()
        list.add(InsnNode(Opcodes.DUP))
        list.add(JumpInsnNode(Opcodes.IFNULL, keep))
        val castTarget = when {
            desc.startsWith("L") && desc.endsWith(";") -> desc.substring(1, desc.length - 1)
            desc.startsWith("[") -> desc
            else -> ""
        }
        if (castTarget.isNotEmpty() && castTarget != "java/lang/Object") {
            list.add(TypeInsnNode(Opcodes.CHECKCAST, castTarget))
        }
        list.add(InsnNode(Opcodes.SWAP))
        list.add(InsnNode(Opcodes.POP))
        list.add(JumpInsnNode(Opcodes.GOTO, end))
        list.add(keep)
        list.add(InsnNode(Opcodes.POP))
        list.add(end)
        return list
    }

    /** 1-slot primitive (I/S/B/C/Z/F)：栈前 [orig:1]，栈后 [orig|unboxed:1]。 */
    private fun buildTrimReturnNarrow(desc: String): InsnList {
        val list = InsnList()
        val keep = LabelNode()
        val end = LabelNode()
        list.add(InsnNode(Opcodes.DUP))
        list.add(JumpInsnNode(Opcodes.IFNULL, keep))
        // [orig:1, ret]  → 用 unbox 出的 primitive 替换 orig
        addUnbox(list, desc)
        // 此时栈 [orig:1, prim:1]
        list.add(InsnNode(Opcodes.SWAP))
        list.add(InsnNode(Opcodes.POP))
        list.add(JumpInsnNode(Opcodes.GOTO, end))
        list.add(keep)
        list.add(InsnNode(Opcodes.POP))   // 弹掉 null dispatch 结果，保留 orig
        list.add(end)
        return list
    }

    /** 2-slot primitive (J/D)：栈前 [orig:2]，栈后 [orig:2|unboxed:2]。 */
    private fun buildTrimReturnWide(desc: String): InsnList {
        val list = InsnList()
        val keep = LabelNode()
        val end = LabelNode()
        list.add(InsnNode(Opcodes.DUP))
        list.add(JumpInsnNode(Opcodes.IFNULL, keep))
        // [orig:2, ret] → unbox → [orig:2, prim:2]
        addUnbox(list, desc)
        // pop orig:2，留 prim:2
        list.add(InsnNode(Opcodes.DUP2_X2))   // [prim:2, orig:2, prim:2]
        list.add(InsnNode(Opcodes.POP2))      // [prim:2, orig:2]
        list.add(InsnNode(Opcodes.POP2))      // [prim:2]
        list.add(JumpInsnNode(Opcodes.GOTO, end))
        list.add(keep)
        list.add(InsnNode(Opcodes.POP))       // 弹掉 null：[orig:2]
        list.add(end)
        return list
    }

    /**
     * 把栈顶的 Object 替换为 primitive：CHECKCAST → invokeVirtual XxxValue。
     * I→intValue, S→shortValue, B→byteValue, C→charValue, Z→booleanValue, F→floatValue,
     * J→longValue, D→doubleValue。
     */
    private fun addUnbox(list: InsnList, desc: String) {
        val (boxed, method, ret) = when (desc) {
            "I" -> Triple("java/lang/Integer",   "intValue",     "()I")
            "S" -> Triple("java/lang/Short",     "shortValue",   "()S")
            "B" -> Triple("java/lang/Byte",      "byteValue",    "()B")
            "C" -> Triple("java/lang/Character", "charValue",    "()C")
            "Z" -> Triple("java/lang/Boolean",   "booleanValue", "()Z")
            "F" -> Triple("java/lang/Float",     "floatValue",   "()F")
            "J" -> Triple("java/lang/Long",      "longValue",    "()J")
            "D" -> Triple("java/lang/Double",    "doubleValue",  "()D")
            else -> return
        }
        list.add(TypeInsnNode(Opcodes.CHECKCAST, boxed))
        list.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, boxed, method, ret, false))
    }

    private fun emitSelfLoad(list: InsnList, site: SiteSpec) {
        if (site.hostMethodDescriptor.isBlank() || site.hostIsStatic) {
            list.add(InsnNode(Opcodes.ACONST_NULL))
        } else {
            list.add(VarInsnNode(Opcodes.ALOAD, 0))
        }
    }

    private fun emitArgsArray(list: InsnList, site: SiteSpec) {
        val hostDesc = site.hostMethodDescriptor
        if (hostDesc.isBlank()) {
            list.add(InsnNode(Opcodes.ICONST_0))
            list.add(TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"))
            return
        }
        val args = Type.getArgumentTypes(hostDesc)
        pushInt(list, args.size)
        list.add(TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"))
        var slot = if (site.hostIsStatic) 0 else 1
        for ((index, type) in args.withIndex()) {
            list.add(InsnNode(Opcodes.DUP))
            pushInt(list, index)
            list.add(loadVarInsn(type, slot))
            boxIfNeeded(list, type)
            list.add(InsnNode(Opcodes.AASTORE))
            slot += type.size
        }
    }

    private fun pushInt(list: InsnList, value: Int) {
        when (value) {
            -1 -> list.add(InsnNode(Opcodes.ICONST_M1))
            0 -> list.add(InsnNode(Opcodes.ICONST_0))
            1 -> list.add(InsnNode(Opcodes.ICONST_1))
            2 -> list.add(InsnNode(Opcodes.ICONST_2))
            3 -> list.add(InsnNode(Opcodes.ICONST_3))
            4 -> list.add(InsnNode(Opcodes.ICONST_4))
            5 -> list.add(InsnNode(Opcodes.ICONST_5))
            in Byte.MIN_VALUE..Byte.MAX_VALUE -> list.add(org.objectweb.asm.tree.IntInsnNode(Opcodes.BIPUSH, value))
            in Short.MIN_VALUE..Short.MAX_VALUE -> list.add(org.objectweb.asm.tree.IntInsnNode(Opcodes.SIPUSH, value))
            else -> list.add(LdcInsnNode(value))
        }
    }

    private fun loadVarInsn(type: Type, slot: Int): VarInsnNode = VarInsnNode(
        when (type.sort) {
            Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> Opcodes.ILOAD
            Type.FLOAT -> Opcodes.FLOAD
            Type.LONG -> Opcodes.LLOAD
            Type.DOUBLE -> Opcodes.DLOAD
            else -> Opcodes.ALOAD
        },
        slot,
    )

    private fun boxIfNeeded(list: InsnList, type: Type) {
        when (type.sort) {
            Type.BOOLEAN -> boxPrimitive(list, "java/lang/Boolean", "(Z)Ljava/lang/Boolean;")
            Type.BYTE -> boxPrimitive(list, "java/lang/Byte", "(B)Ljava/lang/Byte;")
            Type.CHAR -> boxPrimitive(list, "java/lang/Character", "(C)Ljava/lang/Character;")
            Type.SHORT -> boxPrimitive(list, "java/lang/Short", "(S)Ljava/lang/Short;")
            Type.INT -> boxPrimitive(list, "java/lang/Integer", "(I)Ljava/lang/Integer;")
            Type.FLOAT -> boxPrimitive(list, "java/lang/Float", "(F)Ljava/lang/Float;")
            Type.LONG -> boxPrimitive(list, "java/lang/Long", "(J)Ljava/lang/Long;")
            Type.DOUBLE -> boxPrimitive(list, "java/lang/Double", "(D)Ljava/lang/Double;")
            else -> Unit
        }
    }

    private fun boxPrimitive(list: InsnList, owner: String, desc: String) {
        list.add(MethodInsnNode(Opcodes.INVOKESTATIC, owner, "valueOf", desc, false))
    }

    private fun buildTrimSlot(site: SiteSpec): InsnList {
        val list = InsnList()
        val slot = site.trimIndex
        val argDesc = site.trimArgDescriptor
        val skip = LabelNode()
        val end = LabelNode()
        list.add(buildDispatch(site))                             // [ret]
        list.add(InsnNode(Opcodes.DUP))                           // [ret, ret]
        list.add(JumpInsnNode(Opcodes.IFNULL, skip))              // 消费 1 → [ret]
        when {
            argDesc.startsWith("L") || argDesc.startsWith("[") -> {
                val castTarget = when {
                    argDesc.startsWith("L") && argDesc.endsWith(";") -> argDesc.substring(1, argDesc.length - 1)
                    argDesc.startsWith("[") -> argDesc
                    else -> "java/lang/Object"
                }
                list.add(TypeInsnNode(Opcodes.CHECKCAST, castTarget))     // [ret-as-T]
                list.add(VarInsnNode(Opcodes.ASTORE, slot))               // []
            }
            argDesc.isNotEmpty() -> {
                addUnbox(list, argDesc)
                list.add(VarInsnNode(storeOpcodeOf(argDesc), slot))
            }
            else -> {
                list.add(InsnNode(Opcodes.POP))
            }
        }
        list.add(JumpInsnNode(Opcodes.GOTO, end))
        list.add(skip)
        list.add(InsnNode(Opcodes.POP))                           // 弹掉 dup 残留 → []
        list.add(end)
        return list
    }

    private fun storeOpcodeOf(desc: String): Int = when (desc) {
        "J" -> Opcodes.LSTORE
        "D" -> Opcodes.DSTORE
        "F" -> Opcodes.FSTORE
        "I", "S", "B", "C", "Z" -> Opcodes.ISTORE
        else -> Opcodes.ASTORE
    }

    // ---- 旧 visitor API（保留兼容，逐步淘汰） --------------------------------

    /** 发射 dispatcher 调用，栈顶留下 Object 返回值。 */
    fun emitDispatch(mv: MethodVisitor, site: SiteSpec) {
        buildDispatch(site).accept(mv)
    }

    /** GRAFT：发 dispatcher 并丢弃返回值。 */
    fun emitGraft(mv: MethodVisitor, site: SiteSpec) {
        emitDispatch(mv, site)
        mv.visitInsn(Opcodes.POP)
    }

    /** BYPASS：发 dispatcher 取代原指令；若原指令不产生值则 pop 返回值。 */
    fun emitBypass(mv: MethodVisitor, site: SiteSpec, isVoid: Boolean) {
        buildBypass(site, isVoid).accept(mv)
    }

    fun buildConditionalBypass(site: SiteSpec, isVoid: Boolean, afterOriginal: LabelNode): InsnList {
        val tmp = site.bypassTempSlot
        val retDesc = site.bypassReturnDescriptor
        if (tmp < 0 || retDesc.isEmpty()) {
            return InsnList()
        }
        val useOriginal = LabelNode()
        val list = buildBypassDispatch(site)
        list.add(VarInsnNode(Opcodes.ASTORE, tmp))
        list.add(VarInsnNode(Opcodes.ALOAD, tmp))
        list.add(MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "isBypassMiss", "(Ljava/lang/Object;)Z", false))
        list.add(JumpInsnNode(Opcodes.IFNE, useOriginal))
        emitPopConsumed(list, site.bypassConsumedDescs)
        when {
            retDesc == "V" -> Unit
            retDesc.startsWith("L") || retDesc.startsWith("[") -> {
                list.add(VarInsnNode(Opcodes.ALOAD, tmp))
                val castTarget = when {
                    retDesc.startsWith("L") && retDesc.endsWith(";") -> retDesc.substring(1, retDesc.length - 1)
                    else -> retDesc
                }
                if (castTarget != "java/lang/Object") {
                    list.add(TypeInsnNode(Opcodes.CHECKCAST, castTarget))
                }
            }
            retDesc.isNotEmpty() -> {
                list.add(VarInsnNode(Opcodes.ALOAD, tmp))
                addUnbox(list, retDesc)
            }
            isVoid -> Unit
            else -> {
                list.add(VarInsnNode(Opcodes.ALOAD, tmp))
            }
        }
        list.add(JumpInsnNode(Opcodes.GOTO, afterOriginal))
        list.add(useOriginal)
        return list
    }

    /** TRIM 调度入口 —— 详见 [buildTrim]。 */
    fun emitTrim(mv: MethodVisitor, site: SiteSpec) {
        // 直接复用 Tree 形态产物，避免维护两份基本类型 unbox 流水线
        buildTrim(site).accept(mv)
    }

    private fun emitTrimReturn(mv: MethodVisitor, site: SiteSpec) {
        buildTrimReturn(site).accept(mv)
    }

    private fun emitTrimSlot(mv: MethodVisitor, site: SiteSpec) {
        buildTrimSlot(site).accept(mv)
    }
}
