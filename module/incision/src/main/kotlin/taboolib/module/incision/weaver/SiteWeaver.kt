package taboolib.module.incision.weaver

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LookupSwitchInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.MultiANewArrayInsnNode
import org.objectweb.asm.tree.TableSwitchInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode
import taboolib.module.incision.annotation.Trim
import taboolib.module.incision.api.Anchor
import taboolib.module.incision.api.Shift
import taboolib.module.incision.runtime.AdviceKind
import taboolib.module.incision.weaver.site.SiteSpec
import taboolib.module.incision.weaver.site.emitter.DispatcherEmitter
import taboolib.module.incision.weaver.site.matcher.FieldMatcher
import taboolib.module.incision.weaver.site.matcher.InvokeMatcher
import taboolib.module.incision.weaver.site.matcher.MatchEvent
import taboolib.module.incision.weaver.site.matcher.NewMatcher
import taboolib.module.incision.weaver.site.matcher.OpcodeSeqMatcher
import taboolib.module.incision.weaver.site.matcher.TerminalMatcher
import taboolib.module.incision.weaver.site.pattern.SitePattern
import taboolib.module.incision.weaver.site.planner.EmissionPlan
import taboolib.module.incision.weaver.site.planner.PlannerDispatcher
import taboolib.module.incision.weaver.site.recorder.InsnAction
import taboolib.module.incision.weaver.site.recorder.RecordingMethodVisitor
import taboolib.module.incision.weaver.site.recorder.Replayer

/**
 * 站点织入器 —— 组合根。
 *
 * 有两条执行分支，由 [SiteSpec.pattern] / [SiteSpec.offset] / anchor==HEAD 联合决定：
 *  - **streaming（默认）**：无 OpcodeSeq、无非零 offset、无 HEAD 锚点时，直接在 ASM visitor 流中
 *    发射 dispatcher 调用（见 [wrapMethodStreaming]）。
 *  - **recording（扩展）**：遇到以上任一条件时，先录制完整方法指令流，离线 scan → match → plan
 *    → insert → replay（见 [wrapMethodRecording]）。
 *
 * 对外 API 只有 [wrapMethod] 一条；调用方无需关心路径选择，由 sites 自派生。
 */
class SiteWeaver(private val sites: List<SiteSpec>) {

    companion object MetadataResolver {

        /**
         * TRIM RETURN 模式下用 anchor 节点反推栈顶值描述符。详见 [SiteSpec.trimReturnDescriptor]。
         * 对应 streaming 路径的等价入参版本：[resolveTrimReturnDescStreaming]。
         */
        fun resolveTrimReturnDesc(anchor: AbstractInsnNode, hostDesc: String, site: SiteSpec): SiteSpec {
            if (site.kind != AdviceKind.TRIM) return site
            if (site.trimKind != Trim.Kind.RETURN && site.trimKind != null) return site
            if (site.trimReturnDescriptor.isNotEmpty()) return site
            val derived: String = when (anchor) {
                is MethodInsnNode -> returnPart(anchor.desc)
                is FieldInsnNode -> if (anchor.opcode == Opcodes.GETFIELD || anchor.opcode == Opcodes.GETSTATIC) anchor.desc else ""
                is InsnNode -> if (anchor.opcode in Opcodes.IRETURN..Opcodes.ARETURN) returnPart(hostDesc) else ""
                else -> ""
            }
            return if (derived.isNotEmpty()) site.copy(trimReturnDescriptor = derived) else site
        }

        /** streaming 路径的 TRIM RETURN 描述符推断（无 anchor 节点，按 anchor 类型 + 元数据）。 */
        fun resolveTrimReturnDescStreaming(
            anchor: Anchor, opcode: Int, anchorDesc: String, hostDesc: String, site: SiteSpec,
        ): SiteSpec {
            if (site.kind != AdviceKind.TRIM) return site
            if (site.trimKind != Trim.Kind.RETURN && site.trimKind != null) return site
            if (site.trimReturnDescriptor.isNotEmpty()) return site
            val derived: String = when (anchor) {
                Anchor.INVOKE -> returnPart(anchorDesc)
                Anchor.FIELD_GET -> anchorDesc
                Anchor.RETURN -> returnPart(hostDesc)
                else -> ""
            }
            return if (derived.isNotEmpty()) site.copy(trimReturnDescriptor = derived) else site
        }

        /**
         * TRIM ARG：把"逻辑参数序号"翻译成宿主方法的 LV 物理 slot（实例方法 +1，J/D 双宽累加）。
         * 详见 [SiteSpec.trimIndex] / [SiteSpec.trimArgDescriptor]。
         */
        fun resolveTrimSlot(hostAccess: Int, hostDesc: String, site: SiteSpec): SiteSpec {
            if (site.kind != AdviceKind.TRIM || site.trimKind != Trim.Kind.ARG) return site
            val argDescs = parseArgDescriptors(hostDesc)
            val logical = site.trimIndex
            if (logical < 0 || logical >= argDescs.size) return site
            val isStatic = (hostAccess and Opcodes.ACC_STATIC) != 0
            var slot = if (isStatic) 0 else 1
            for (i in 0 until logical) slot += slotWidth(argDescs[i])
            val patched = if (site.trimArgDescriptor.isEmpty()) argDescs[logical] else site.trimArgDescriptor
            return site.copy(trimIndex = slot, trimArgDescriptor = patched)
        }

        /** Tree 路径：BYPASS 元数据（消费描述符 + 返回描述符 + tmp slot）。 */
        fun resolveBypassMeta(anchor: AbstractInsnNode, hostMaxLocals: Int, site: SiteSpec): SiteSpec {
            if (site.kind != AdviceKind.BYPASS) return site
            val (consumed, ret) = when (anchor) {
                is MethodInsnNode -> {
                    val args = parseArgDescriptors(anchor.desc).joinToString("")
                    val receiver = if (anchor.opcode == Opcodes.INVOKESTATIC) "" else "L${anchor.owner};"
                    receiver + args to returnPart(anchor.desc)
                }
                is FieldInsnNode -> when (anchor.opcode) {
                    Opcodes.GETFIELD -> "L${anchor.owner};" to anchor.desc
                    Opcodes.GETSTATIC -> "" to anchor.desc
                    Opcodes.PUTFIELD -> "L${anchor.owner};${anchor.desc}" to "V"
                    Opcodes.PUTSTATIC -> anchor.desc to "V"
                    else -> "" to ""
                }
                else -> "" to ""
            }
            if (ret.isEmpty()) return site
            return site.copy(bypassConsumedDescs = consumed, bypassReturnDescriptor = ret, bypassTempSlot = hostMaxLocals)
        }

        /** Streaming 路径：BYPASS 元数据（按 anchor opcode + 元数据，tmp slot 由调用方提供）。 */
        fun resolveBypassMetaStreaming(
            anchor: Anchor, opcode: Int, anchorOwner: String, anchorDesc: String,
            tmpSlot: Int, site: SiteSpec,
        ): SiteSpec {
            if (site.kind != AdviceKind.BYPASS) return site
            val (consumed, ret) = when (anchor) {
                Anchor.INVOKE -> {
                    val args = parseArgDescriptors(anchorDesc).joinToString("")
                    val receiver = if (opcode == Opcodes.INVOKESTATIC) "" else "L$anchorOwner;"
                    receiver + args to returnPart(anchorDesc)
                }
                Anchor.FIELD_GET -> when (opcode) {
                    Opcodes.GETFIELD -> "L$anchorOwner;" to anchorDesc
                    Opcodes.GETSTATIC -> "" to anchorDesc
                    else -> "" to ""
                }
                Anchor.FIELD_PUT -> when (opcode) {
                    Opcodes.PUTFIELD -> "L$anchorOwner;$anchorDesc" to "V"
                    Opcodes.PUTSTATIC -> anchorDesc to "V"
                    else -> "" to ""
                }
                else -> "" to ""
            }
            if (ret.isEmpty()) return site
            return site.copy(bypassConsumedDescs = consumed, bypassReturnDescriptor = ret, bypassTempSlot = tmpSlot)
        }

        private fun returnPart(methodDesc: String): String {
            val close = methodDesc.indexOf(')')
            return if (close in 0 until methodDesc.length - 1) methodDesc.substring(close + 1) else ""
        }

        fun parseArgDescriptors(methodDesc: String): List<String> {
            val open = methodDesc.indexOf('(')
            val close = methodDesc.indexOf(')')
            if (open < 0 || close <= open) return emptyList()
            val args = methodDesc.substring(open + 1, close)
            val out = mutableListOf<String>()
            var i = 0
            while (i < args.length) {
                val start = i
                while (i < args.length && args[i] == '[') i++
                when (args.getOrNull(i)) {
                    'L' -> {
                        val semi = args.indexOf(';', i)
                        if (semi < 0) return out
                        out += args.substring(start, semi + 1); i = semi + 1
                    }
                    null -> return out
                    else -> { out += args.substring(start, i + 1); i++ }
                }
            }
            return out
        }

        fun slotWidth(d: String): Int = if (d == "J" || d == "D") 2 else 1

        /** 估算 streaming 路径下足以放 tmp 的安全 slot：宿主参数(含 this)总宽度。ClassWriter COMPUTE_MAXS 会再补齐。 */
        fun estimateBypassTmpSlot(hostAccess: Int, hostDesc: String): Int {
            val base = if ((hostAccess and Opcodes.ACC_STATIC) != 0) 0 else 1
            return parseArgDescriptors(hostDesc).fold(base) { acc, d -> acc + slotWidth(d) }
        }

        fun resolveDispatchContext(hostAccess: Int, hostDesc: String, site: SiteSpec): SiteSpec {
            val hostIsStatic = (hostAccess and Opcodes.ACC_STATIC) != 0
            if (site.hostMethodDescriptor == hostDesc && site.hostIsStatic == hostIsStatic) return site
            return site.copy(hostMethodDescriptor = hostDesc, hostIsStatic = hostIsStatic)
        }
    }

    private val hasOpcodeSeq: Boolean = sites.any { it.pattern is SitePattern.OpcodeSeq }
    private val hasNonZeroOffset: Boolean = sites.any { it.offset != 0 }
    private val hasHead: Boolean = sites.any { it.anchor == Anchor.HEAD }

    fun wrapMethod(
        api: Int,
        delegate: MethodVisitor,
        access: Int = 0,
        name: String = "",
        descriptor: String = "",
        signature: String? = null,
        exceptions: Array<out String>? = null,
    ): MethodVisitor {
        if (sites.isEmpty()) return delegate
        return if (shouldStream()) wrapMethodStreaming(api, delegate, access, descriptor)
        else wrapMethodRecording(api, delegate, access, name, descriptor, signature, exceptions)
    }

    private fun shouldStream(): Boolean =
        PlannerDispatcher.canStream(sites, hasOpcodeSeq) && !hasNonZeroOffset && !hasHead

    private fun wrapMethodStreaming(api: Int, delegate: MethodVisitor, access: Int, descriptor: String): MethodVisitor =
        SiteVisitor(api, delegate, sites, access, descriptor)

    /**
     * recording 路径 — Tree API 形态：把方法完整收进 [MethodNode]，在 visitEnd 时按
     * [EmissionPlan] 把 [InsnList] 原位 insert 到 [MethodNode.instructions]；随后 `accept(delegate)`
     * 回放到下游。节点引用天然稳定，插入不影响其他 plan 的锚点。
     *
     * 相比旧的 record→InsnAction→Replayer 链路，本路径：
     *  1. 不再显式重演 `visitFrame` / 自造 Label —— MethodNode 自身负责 tree ↔ visitor 双向转换
     *  2. 发射的 [InsnList] 由 [DispatcherEmitter.buildGraft] 等产出，Label 以 [org.objectweb.asm.tree.LabelNode] 形态
     *     进入 tree，ClassWriter 的 COMPUTE_FRAMES 能正常推导
     */
    private fun wrapMethodRecording(
        api: Int,
        delegate: MethodVisitor,
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor =
        TreeMethodDriver(api, access, name, descriptor, signature, exceptions, delegate, sites)

    /**
     * streaming 路径内核。
     *
     * 关键转变：所有 BYPASS / TRIM / GRAFT 的字节码发射都委托给 [DispatcherEmitter.build*]
     * 然后 `accept(mv)` 回放，与 [TreeMethodDriver] 完全共享一份发射流水线 —— 避免双维护
     * 导致 #34/#36/#37 这类 fix 只在 recording 路径生效、streaming 路径 silently 走老逻辑。
     *
     * 元数据补齐通过 [MetadataResolver.resolve*Streaming] 完成，依赖宿主方法的 `access` /
     * `descriptor`（this 偏移、参数物理 slot、tmp slot 估算）。
     */
    private class SiteVisitor(
        api: Int,
        mv: MethodVisitor,
        rawSites: List<SiteSpec>,
        private val hostAccess: Int,
        private val hostDesc: String,
    ) : MethodVisitor(api, mv) {

        // 防御性去重：同一 anchor+pattern+kind+target+shift+ordinal 完全一致的 SiteSpec 重复入队会让
        // ordinal 过滤失效（每条独立计数 → 多 hit）。Scalpel 累积路径可能产生重复，先收敛。
        // 注意：仅 `SiteSpec` 所有字段相等才视为同一条，不会误合并 kind/target 不同的 advice。
        private val sites: List<SiteSpec> = rawSites.distinct()

        // per-site 计数器 —— 每条 SiteSpec 独立推进 idx。不同 site 即使同 target 也互不影响，
        // 避免"共享 idx"模型下 ordinal=-1 的 advice 把 idx 提前推掉导致 ordinal>=0 的兄弟丢匹配。
        private val counters = IntArray(sites.size)

        // 每个宿主方法预留一个 tmp slot 给 BYPASS 用（保存 dispatch 返回 Object）。COMPUTE_MAXS 会再补齐。
        private val bypassTmpSlot: Int = MetadataResolver.estimateBypassTmpSlot(hostAccess, hostDesc)

        override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
            val matched = matchFor(Anchor.INVOKE, owner, name, descriptor)
            if (matched.isEmpty()) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                return
            }
            val isVoid = descriptor.endsWith(")V")
            val enriched = matched.map { enrich(it, Anchor.INVOKE, opcode, owner, descriptor) }
            applySites(enriched, isVoid) { super.visitMethodInsn(opcode, owner, name, descriptor, isInterface) }
        }

        override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
            val anchor = when (opcode) {
                Opcodes.GETFIELD, Opcodes.GETSTATIC -> Anchor.FIELD_GET
                Opcodes.PUTFIELD, Opcodes.PUTSTATIC -> Anchor.FIELD_PUT
                else -> null
            }
            if (anchor == null) {
                super.visitFieldInsn(opcode, owner, name, descriptor)
                return
            }
            val matched = matchFor(anchor, owner, name, descriptor)
            if (matched.isEmpty()) {
                super.visitFieldInsn(opcode, owner, name, descriptor)
                return
            }
            val isVoid = anchor == Anchor.FIELD_PUT
            val enriched = matched.map { enrich(it, anchor, opcode, owner, descriptor) }
            applySites(enriched, isVoid) { super.visitFieldInsn(opcode, owner, name, descriptor) }
        }

        override fun visitTypeInsn(opcode: Int, type: String) {
            if (opcode != Opcodes.NEW) {
                super.visitTypeInsn(opcode, type)
                return
            }
            val matched = matchFor(Anchor.NEW, type, "", "")
            if (matched.isEmpty()) {
                super.visitTypeInsn(opcode, type)
                return
            }
            val enriched = matched.map { enrich(it, Anchor.NEW, opcode, type, "") }
            applySites(enriched, isVoid = false) { super.visitTypeInsn(opcode, type) }
        }

        override fun visitInsn(opcode: Int) {
            val anchor = when {
                opcode == Opcodes.ATHROW -> Anchor.THROW
                opcode in Opcodes.IRETURN..Opcodes.RETURN -> Anchor.RETURN
                else -> null
            }
            if (anchor == null) {
                super.visitInsn(opcode)
                return
            }
            val matched = matchFor(anchor, "", "", "")
            if (matched.isEmpty()) {
                super.visitInsn(opcode)
                return
            }
            val enriched = matched.map { enrich(it, anchor, opcode, "", "") }
            // RETURN/THROW 是消费栈顶的终止指令——TRIM 必须在 emitOriginal 之前发射，否则
            // 落到 unreachable 代码区，Analyzer 视作空栈，DUP 立刻 "Cannot pop operand off an empty stack"
            applySites(enriched, isVoid = true, terminalConsumer = true) { super.visitInsn(opcode) }
        }

        /**
         * per-site 计数：每条 site 独立推进自己的 idx；ordinal=-1 通配，否则精确匹配第 N 次命中。
         */
        private fun matchFor(anchor: Anchor, owner: String, name: String, descriptor: String): List<SiteSpec> {
            var out: MutableList<SiteSpec>? = null
            for (i in sites.indices) {
                val site = sites[i]
                if (site.anchor != anchor) continue
                val ownerOk = site.ownerPattern.isEmpty() || site.ownerPattern == owner
                val nameOk = site.namePattern.isEmpty() || site.namePattern == name
                val descOk = site.descPattern.isEmpty() || site.descPattern == descriptor
                if (!ownerOk || !nameOk || !descOk) continue
                val idx = counters[i]
                counters[i] = idx + 1
                if (site.ordinal < 0 || site.ordinal == idx) {
                    if (out == null) out = mutableListOf()
                    out.add(site)
                }
            }
            return out ?: emptyList()
        }

        /** 把 raw SiteSpec 喂给 MetadataResolver 三件套，得到带元数据的版本供 emitter 消费。 */
        private fun enrich(site: SiteSpec, anchor: Anchor, opcode: Int, anchorOwner: String, anchorDesc: String): SiteSpec {
            var s = site
            s = MetadataResolver.resolveDispatchContext(hostAccess, hostDesc, s)
            s = MetadataResolver.resolveTrimReturnDescStreaming(anchor, opcode, anchorDesc, hostDesc, s)
            s = MetadataResolver.resolveTrimSlot(hostAccess, hostDesc, s)
            s = MetadataResolver.resolveBypassMetaStreaming(anchor, opcode, anchorOwner, anchorDesc, bypassTmpSlot, s)
            return s
        }

        private fun applySites(
            matched: List<SiteSpec>,
            isVoid: Boolean,
            terminalConsumer: Boolean = false,
            emitOriginal: () -> Unit,
        ) {
            var bypass: SiteSpec? = null
            val before = mutableListOf<SiteSpec>()
            val after = mutableListOf<SiteSpec>()
            val trims = mutableListOf<SiteSpec>()
            for (s in matched) {
                when (s.kind) {
                    AdviceKind.GRAFT -> if (s.shift == Shift.AFTER) after += s else before += s
                    AdviceKind.BYPASS -> if (bypass == null) bypass = s
                    AdviceKind.TRIM -> trims += s
                    else -> { /* 其他 kind 由 AdviceAdapter 路径处理 */ }
                }
            }
            for (s in before) {
                DispatcherEmitter.buildGraft(s).accept(mv)
                emittedCount++
            }
            val b = bypass
            if (b != null) {
                DispatcherEmitter.buildBypass(b, isVoid).accept(mv)
                emittedCount++
            } else if (terminalConsumer) {
                // RETURN/THROW：trim 先于 emitOriginal —— 在终止消费指令之前拦截栈顶值
                for (s in trims) {
                    DispatcherEmitter.buildTrim(s).accept(mv)
                    emittedCount++
                }
                emitOriginal()
            } else {
                emitOriginal()
                for (s in trims) {
                    DispatcherEmitter.buildTrim(s).accept(mv)
                    emittedCount++
                }
            }
            for (s in after) {
                DispatcherEmitter.buildGraft(s).accept(mv)
                emittedCount++
            }
        }

        // streaming 路径下，所有发射加起来的栈/局部冲击：dispatch 自身 +3、TRIM wide +4、BYPASS ALOAD tmp +1。
        // 取保守上界 +16/emission；下游 ClassWriter COMPUTE_MAXS 在最终写出时再精修。
        private var emittedCount: Int = 0

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            // FrameVerifier 走 Analyzer<BasicValue>，按下游 MethodNode.maxStack 预分配 Frame.values[]，
            // 我们 streaming 注入的 dispatch/TRIM/BYPASS 序列若超过原 maxStack 即 push 越界。直接抬到
            // max(原值 + 16*emission, 1024) 上界，避免 emission 计数估漏；ClassWriter COMPUTE_MAXS 不会
            // 因为更大的 maxStack 而产生更宽的 frame，仍按真实使用量精修最终值。
            val needed = maxOf(maxStack + 16 * emittedCount, if (emittedCount > 0) 1024 else maxStack)
            val newLocals = maxOf(maxLocals, bypassTmpSlot + 1)
            super.visitMaxs(maxOf(maxStack, needed), newLocals)
        }
    }

    /**
     * Tree API 形态驱动：继承 [MethodNode]（本身是 `MethodVisitor`，收完就是完整指令树），
     * visitEnd 时跑 scan → match → plan → insert → accept(downstream)。
     *
     * 相比旧 [RecordingDriver]：
     *  - 不再通过 InsertEmission 的 replay 路径回写字节码；发射产物直接是 [InsnList]
     *  - Label 以 [org.objectweb.asm.tree.LabelNode] 节点形式嵌入 `instructions`，
     *    ClassWriter 的 `COMPUTE_FRAMES` 能对其做正常推导，彻底消除原 recording 路径的 Frame.merge 崩溃
     *  - 节点引用稳定，插入不影响其他 plan 的锚点，免去"倒序插入"技巧
     */
    private class TreeMethodDriver(
        api: Int,
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?,
        private val downstream: MethodVisitor,
        rawSites: List<SiteSpec>,
    ) : MethodNode(api, access, name, descriptor, signature, exceptions) {

        // 与 streaming 路径 SiteVisitor 保持一致：防御性去重同一锚点+kind+target+shift+ordinal 完全相等的
        // SiteSpec 重复入队。Scalpel 累积路径曾观察到同一 advice 被注册多次，导致每锚点插入 N 份发射 →
        // 对应 advice 命中计数被 ×N 放大（曾出现 expected=2 actual=6 的 3x 放大）。
        private val sites: List<SiteSpec> = rawSites.distinct()

        override fun visitEnd() {
            super.visitEnd()
            try {
                runTreePipeline()
            } catch (t: Throwable) {
                taboolib.module.incision.diagnostic.Forensics.error(
                    "SiteWeaver tree pipeline failed: ${t::class.java.name}: ${t.message} " +
                        "| method=$name$desc sites=${sites.size}; fallback to raw accept",
                    t,
                )
            }
            this.accept(downstream)
        }

        /**
         * 扫遍 `instructions`，收集 [MatchEvent] → [EmissionPlan]，按 plan 策略把 [InsnList] 插入。
         *
         * 顺序说明：plans 按遇到锚点的顺序处理。同一锚点上多个 BEFORE plan，后加入者离 anchor 更近
         * （与 streaming 路径 `for (s in before) emitGraft(s)` 的语义一致）；AFTER 同样。offset != 0 的
         * plan 落在邻居节点上，彼此不相冲。
         */
        private fun runTreePipeline() {
            val plainSites = sites.filter { it.pattern !is SitePattern.OpcodeSeq }
            val opcodeSeqSites = sites.filter { it.pattern is SitePattern.OpcodeSeq }
            val invokeMatcher = InvokeMatcher(plainSites)
            val fieldGetMatcher = FieldMatcher(plainSites, Anchor.FIELD_GET)
            val fieldPutMatcher = FieldMatcher(plainSites, Anchor.FIELD_PUT)
            val newMatcher = NewMatcher(plainSites)
            val terminalMatcher = TerminalMatcher(plainSites)

            val opcodeSeqEntries: List<OpcodeSeqMatcher.Entry> = opcodeSeqSites.mapNotNull { s ->
                val p = s.pattern
                if (p is SitePattern.OpcodeSeq) OpcodeSeqMatcher.Entry(s, p.steps) else null
            }
            val opcodeSeqMatcher = OpcodeSeqMatcher(opcodeSeqEntries)
            val planner = PlannerDispatcher()

            data class NodePlan(val anchor: AbstractInsnNode, val plan: EmissionPlan)
            val plans = ArrayList<NodePlan>()

            val nodes = instructions.toArray()
            for (anchor in nodes) {
                when (anchor) {
                    is MethodInsnNode -> for (ev in invokeMatcher.match(anchor.owner, anchor.name, anchor.desc))
                        plans += NodePlan(anchor, planner.plan(ev))
                    is FieldInsnNode -> {
                        val m = when (anchor.opcode) {
                            Opcodes.GETFIELD, Opcodes.GETSTATIC -> fieldGetMatcher
                            Opcodes.PUTFIELD, Opcodes.PUTSTATIC -> fieldPutMatcher
                            else -> null
                        }
                        if (m != null) for (ev in m.match(anchor.owner, anchor.name, anchor.desc))
                            plans += NodePlan(anchor, planner.plan(ev))
                    }
                    is TypeInsnNode -> if (anchor.opcode == Opcodes.NEW) for (ev in newMatcher.match(anchor.desc))
                        plans += NodePlan(anchor, planner.plan(ev))
                    is InsnNode -> for (ev in terminalMatcher.matchInsn(anchor.opcode))
                        plans += NodePlan(anchor, planner.plan(ev))
                    else -> Unit
                }
            }

            if (opcodeSeqEntries.isNotEmpty()) {
                val indexedViews = buildRealInsnViews(nodes)
                val seqEvents = opcodeSeqMatcher.match(indexedViews.views)
                for (ev in seqEvents) {
                    val idx = indexedViews.indices.getOrNull(ev.anchorIndex) ?: continue
                    if (idx in nodes.indices) plans += NodePlan(nodes[idx], planner.plan(ev))
                }
            }

            val headEvents = terminalMatcher.matchHead()
            val headAnchor = findHeadInsertionAnchor(nodes)

            // 同 anchor 多 plan 的处理顺序：把 BYPASS 排到最后。
            // 原因：BYPASS 的实现是 insertBefore(anchor, em) + remove(anchor) —— 一旦先于 GRAFT AFTER
            // 处理，anchor 就被移出 InsnList，后续 `instructions.insert(anchor, em)` 落到孤儿节点上
            // 静默失败，导致 GRAFT AFTER / TRIM 的 emission 丢失（曾观察到 surgeon-graft-invoke-after
            // 命中 1 次而非期望的 2 次）。把 BYPASS 排最后后：
            //   1) GRAFT BEFORE → 插在 anchor 之前
            //   2) GRAFT AFTER / TRIM → 插在 anchor 之后（成为 anchor 的兄弟节点）
            //   3) BYPASS → insertBefore(anchor) + remove(anchor) —— 兄弟节点不受影响，最终顺序：
            //      ..., before-graft, bypass-emission, anchor-after-graft/trim, ...
            // 同 anchor 内 BYPASS 仅取首个（applySites 流式路径已是该语义）。
            val sortedPlans = plans.sortedBy { if (it.plan.site.kind == AdviceKind.BYPASS) 1 else 0 }
            for (np in sortedPlans) applyPlan(np.anchor, np.plan)
            // 每个 emission 对栈的最大额外消耗：dispatch 本身 +3；TRIM RETURN wide DUP2_X2 路径 +4；
            // BYPASS ASTORE/ALOAD tmp +1。取保守上界 +8 每个 site，避免 Analyzer/ClassWriter 在用旧
            // maxStack 初始化 Frame 时 push 越界（曾观察到 Frame.push: Insufficient maximum stack size
            // 与 Frame.merge Index -1 out of bounds 两类失败）。COMPUTE_MAXS 仍会精修最终值。
            var insertedCount = plans.size
            if (headEvents.isNotEmpty() && headAnchor != null) {
                // HEAD 语义：方法最开头一次性插入；多条按声明顺序。
                // 但如果方法头已经有 AdviceInjector 生成的 whole-method @LEAD/@SPLICE 调度块，
                // HEAD site 必须插在这段入口块之后，否则 HEAD @Trim 会先改写 args，
                // 让 whole-method @Splice.resume.proceed() 看到的已是被污染后的参数。
                // 注意：必须经过 MetadataResolver 富集——TRIM ARG 的 trimIndex 在 SurgeonScanner 里是
                // 逻辑参数序号（0/1/2...），需要按宿主 access/desc 翻译成物理 LV slot，否则会 ASTORE
                // 到 slot 0 覆盖 `this`，引发 PUTFIELD VerifyError。
                for (ev in headEvents) {
                    var site = ev.siteSpec
                    site = MetadataResolver.resolveDispatchContext(access, desc, site)
                    site = MetadataResolver.resolveTrimReturnDesc(headAnchor, desc, site)
                    site = MetadataResolver.resolveTrimSlot(access, desc, site)
                    val emission = buildEmission(site, isVoid = true)
                    instructions.insertBefore(headAnchor, emission)
                    insertedCount++
                }
            }
            if (insertedCount > 0) {
                // FrameVerifier 走 Analyzer<BasicValue>，按 m.maxStack 预分配 Frame.values[]，
                // push 越界即 IndexOutOfBoundsException。我们既不知道每个 emission 在该方法中的精确
                // 栈深度叠加，也不能让预检卡住——保守把 maxStack 抬到 1024 上界，ClassWriter 的
                // COMPUTE_MAXS 在最终写出时仍会精修到实际最小值。
                val needed = maxOf(this.maxStack + 16 * insertedCount, 1024)
                if (needed > this.maxStack) this.maxStack = needed
                // BYPASS 把 tmp slot 分配在 hostMaxLocals（第一个空闲槽），但 ASTORE 到该槽必须先把
                // MethodNode.maxLocals 抬上去，否则 Analyzer 立刻 "Trying to set an inexistant local variable N"。
                // 留 +2 兜底（ALOAD 2-slot 类型）；COMPUTE_MAXS 仍会按真实使用量精修。
                val bypassPresent = sites.any { it.kind == AdviceKind.BYPASS }
                if (bypassPresent) this.maxLocals = this.maxLocals + 2
            }
        }

        private fun applyPlan(anchor: AbstractInsnNode, plan: EmissionPlan) {
            var site = plan.site
            site = MetadataResolver.resolveDispatchContext(access, desc, site)
            site = MetadataResolver.resolveTrimReturnDesc(anchor, desc, site)
            site = MetadataResolver.resolveTrimSlot(access, desc, site)
            site = MetadataResolver.resolveBypassMeta(anchor, maxLocals, site)
            val absOffset = kotlin.math.abs(site.offset)
            val isVoid = isAnchorVoid(anchor)
            when (plan.strategy) {
                EmissionPlan.Strategy.IMMEDIATE -> insertAtAnchor(anchor, site, buildEmission(site, isVoid), isVoid)
                EmissionPlan.Strategy.DEFERRED -> {
                    val emission = buildEmission(site, isVoid)
                    val target = skipForward(anchor, absOffset)
                    instructions.insert(target, emission)
                }
                EmissionPlan.Strategy.BUFFERING -> {
                    val emission = buildEmission(site, isVoid)
                    val target = skipBackward(anchor, absOffset)
                    instructions.insertBefore(target, emission)
                }
            }
        }

        /**
         * IMMEDIATE 就地插入：
         *  - GRAFT: shift=AFTER 插入 anchor 之后，否则之前
         *  - TRIM: 默认插入 anchor 之后（在锚点产生的栈顶值上做拦截）；当 anchor 是 RETURN/ATHROW
         *    这类**消费栈顶**的终止指令时，必须改为插入之前——否则 emission 落在 unreachable
         *    代码区，DUP 立刻 "Cannot pop operand off an empty stack"
         *  - BYPASS: 用发射的 InsnList 替换 anchor 原指令
         *  - 其他: 插入之前（SPLICE/EXCISE 目前不会进 recording 路径，兜底 BEFORE）
         */
        private fun insertAtAnchor(anchor: AbstractInsnNode, site: SiteSpec, em: InsnList, isVoid: Boolean) {
            when (site.kind) {
                AdviceKind.GRAFT -> if (site.shift == Shift.AFTER) instructions.insert(anchor, em)
                                    else instructions.insertBefore(anchor, em)
                AdviceKind.TRIM -> {
                    if (isTerminalConsumer(anchor)) instructions.insertBefore(anchor, em)
                    else instructions.insert(anchor, em)
                }
                AdviceKind.BYPASS -> {
                    val afterOriginal = LabelNode()
                    instructions.insertBefore(anchor, DispatcherEmitter.buildConditionalBypass(site, isVoid, afterOriginal))
                    instructions.insert(anchor, afterOriginal)
                }
                else -> instructions.insertBefore(anchor, em)
            }
        }

        private fun buildEmission(site: SiteSpec, isVoid: Boolean): InsnList = when (site.kind) {
            AdviceKind.GRAFT -> DispatcherEmitter.buildGraft(site)
            AdviceKind.BYPASS -> DispatcherEmitter.buildBypass(site, isVoid)
            AdviceKind.TRIM -> DispatcherEmitter.buildTrim(site)
            else -> DispatcherEmitter.buildGraft(site)
        }

        private fun isAnchorVoid(n: AbstractInsnNode): Boolean = when (n) {
            is MethodInsnNode -> n.desc.endsWith(")V")
            is FieldInsnNode -> n.opcode == Opcodes.PUTFIELD || n.opcode == Opcodes.PUTSTATIC
            is InsnNode -> n.opcode == Opcodes.ATHROW || n.opcode in Opcodes.IRETURN..Opcodes.RETURN
            else -> false
        }

        /**
         * 终止消费者：anchor 自身**消费**栈顶值（IRETURN..ARETURN / ATHROW）。
         * TRIM 这类需要先于栈顶值消费做拦截的 emission，必须插在这种 anchor **之前**，
         * 否则插到之后即落入 unreachable 区，Analyzer 视作空栈，DUP/SWAP 全部立即崩。
         * 注意 RETURN（void）也属于消费者，但栈顶 = 空，TRIM 在 void 场景下无意义。
         */
        private fun isTerminalConsumer(n: AbstractInsnNode): Boolean = n is InsnNode && (
            n.opcode == Opcodes.ATHROW || n.opcode in Opcodes.IRETURN..Opcodes.ARETURN
        )

        /** 向前越过 n 条真实指令（opcode >= 0 排除 Label/Frame/LineNumber）。 */
        private fun skipForward(from: AbstractInsnNode, n: Int): AbstractInsnNode {
            if (n <= 0) return from
            var cur: AbstractInsnNode = from
            var left = n
            while (cur.next != null && left > 0) {
                cur = cur.next
                if (cur.opcode >= 0) left--
            }
            return cur
        }

        private fun skipBackward(from: AbstractInsnNode, n: Int): AbstractInsnNode {
            if (n <= 0) return from
            var cur: AbstractInsnNode = from
            var left = n
            while (cur.previous != null && left > 0) {
                cur = cur.previous
                if (cur.opcode >= 0) left--
            }
            return cur
        }

        private data class IndexedInsnViews(
            val indices: List<Int>,
            val views: List<OpcodeSeqMatcher.InsnView>,
        )

        private fun buildRealInsnViews(nodes: Array<AbstractInsnNode>): IndexedInsnViews {
            val indices = ArrayList<Int>(nodes.size)
            val views = ArrayList<OpcodeSeqMatcher.InsnView>(nodes.size)
            for ((index, node) in nodes.withIndex()) {
                val view = toInsnView(node) ?: continue
                indices += index
                views += view
            }
            return IndexedInsnViews(indices, views)
        }

        private fun toInsnView(n: AbstractInsnNode): OpcodeSeqMatcher.InsnView? {
            return when (n) {
                is InsnNode -> OpcodeSeqMatcher.InsnView(n.opcode)
                is IntInsnNode -> OpcodeSeqMatcher.InsnView(n.opcode)
                is VarInsnNode -> OpcodeSeqMatcher.InsnView(n.opcode)
                is TypeInsnNode -> OpcodeSeqMatcher.InsnView(n.opcode, owner = n.desc)
                is FieldInsnNode -> OpcodeSeqMatcher.InsnView(n.opcode, n.owner, n.name, n.desc)
                is MethodInsnNode -> OpcodeSeqMatcher.InsnView(n.opcode, n.owner, n.name, n.desc)
                is LdcInsnNode -> OpcodeSeqMatcher.InsnView(Opcodes.LDC, cst = n.cst)
                is JumpInsnNode -> OpcodeSeqMatcher.InsnView(n.opcode)
                is IincInsnNode -> OpcodeSeqMatcher.InsnView(Opcodes.IINC)
                is TableSwitchInsnNode -> OpcodeSeqMatcher.InsnView(Opcodes.TABLESWITCH)
                is LookupSwitchInsnNode -> OpcodeSeqMatcher.InsnView(Opcodes.LOOKUPSWITCH)
                is MultiANewArrayInsnNode -> OpcodeSeqMatcher.InsnView(Opcodes.MULTIANEWARRAY, owner = n.desc)
                is InvokeDynamicInsnNode -> OpcodeSeqMatcher.InsnView(Opcodes.INVOKEDYNAMIC, name = n.name, descriptor = n.desc)
                else -> null
            }
        }

        private fun findHeadInsertionAnchor(nodes: Array<AbstractInsnNode>): AbstractInsnNode? {
            var anchor = nodes.firstOrNull { it.opcode >= 0 } ?: return null
            while (true) {
                val next = skipGeneratedEntryDispatch(anchor) ?: break
                anchor = next
            }
            return anchor
        }

        private fun skipGeneratedEntryDispatch(start: AbstractInsnNode): AbstractInsnNode? {
            if (start !is LdcInsnNode || start.cst !is String) return null
            var cursor: AbstractInsnNode? = start
            var scannedReal = 0
            var dispatchCall: MethodInsnNode? = null
            while (cursor != null && scannedReal < 48) {
                if (cursor.opcode >= 0) scannedReal++
                if (cursor is MethodInsnNode &&
                    cursor.owner == "io/izzel/incision/bridge/IncisionBridge" &&
                    cursor.name == "dispatch"
                ) {
                    dispatchCall = cursor
                    break
                }
                cursor = cursor.next
            }
            val call = dispatchCall ?: return null
            val nextReal = nextRealInsn(call.next) ?: return null
            if (nextReal is InsnNode && nextReal.opcode == Opcodes.POP) {
                return nextRealInsn(nextReal.next)
            }
            if (nextReal is InsnNode && nextReal.opcode == Opcodes.DUP) {
                val ifNull = nextRealInsn(nextReal.next) as? JumpInsnNode ?: return null
                if (ifNull.opcode != Opcodes.IFNULL) return null
                var cursorAfterLabel: AbstractInsnNode? = ifNull.label
                while (cursorAfterLabel != null) {
                    if (cursorAfterLabel is InsnNode && cursorAfterLabel.opcode == Opcodes.POP) {
                        return nextRealInsn(cursorAfterLabel.next)
                    }
                    cursorAfterLabel = cursorAfterLabel.next
                }
            }
            return null
        }

        private fun nextRealInsn(from: AbstractInsnNode?): AbstractInsnNode? {
            var cursor = from
            while (cursor != null && cursor.opcode < 0) cursor = cursor.next
            return cursor
        }
    }

    /**
     * recording 路径驱动：继承 [RecordingMethodVisitor]（next=null，禁双发），visitEnd 时运行
     *   scan → match → plan → insert → replay(downstream)。
     */
    private class RecordingDriver(
        api: Int,
        private val downstream: MethodVisitor,
        private val sites: List<SiteSpec>,
    ) : RecordingMethodVisitor(api, next = null) {

        override fun visitEnd() {
            super.visitEnd()
            try {
                runPipeline()
            } catch (t: Throwable) {
                taboolib.module.incision.diagnostic.Forensics.error(
                    "SiteWeaver recording pipeline failed: ${t::class.java.name}: ${t.message} | sites=${sites.size} actions=${actions.size}; fallback to raw replay",
                    t,
                )
                Replayer(actions).replay(downstream)
            }
        }

        private fun runPipeline() {
            val actions = this.actions
            val replayer = Replayer(actions)
            val planner = PlannerDispatcher()

            val plainSites = sites.filter { it.pattern !is SitePattern.OpcodeSeq }
            val opcodeSeqSites = sites.filter { it.pattern is SitePattern.OpcodeSeq }
            val invokeMatcher = InvokeMatcher(plainSites)
            val fieldGetMatcher = FieldMatcher(plainSites, Anchor.FIELD_GET)
            val fieldPutMatcher = FieldMatcher(plainSites, Anchor.FIELD_PUT)
            val newMatcher = NewMatcher(plainSites)
            val terminalMatcher = TerminalMatcher(plainSites)

            val opcodeSeqEntries: List<OpcodeSeqMatcher.Entry> = opcodeSeqSites.mapNotNull { s ->
                val p = s.pattern
                if (p is SitePattern.OpcodeSeq) OpcodeSeqMatcher.Entry(s, p.steps) else null
            }
            val opcodeSeqMatcher = OpcodeSeqMatcher(opcodeSeqEntries)

            data class IndexedPlan(val index: Int, val plan: EmissionPlan)
            val indexedPlans = ArrayList<IndexedPlan>()

            fun addPlans(anchorIdx: Int, events: List<MatchEvent>) {
                for (ev in events) {
                    val evWithIdx = if (ev.anchorIndex >= 0) ev else MatchEvent(ev.siteSpec, anchorIdx)
                    indexedPlans += IndexedPlan(anchorIdx, planner.plan(evWithIdx))
                }
            }

            actions.forEachIndexed { idx, a ->
                when (a) {
                    is InsnAction.MethodInsn -> addPlans(idx, invokeMatcher.match(a.owner, a.name, a.descriptor))
                    is InsnAction.FieldInsn -> {
                        val m = when (a.opcode) {
                            Opcodes.GETFIELD, Opcodes.GETSTATIC -> fieldGetMatcher
                            Opcodes.PUTFIELD, Opcodes.PUTSTATIC -> fieldPutMatcher
                            else -> null
                        }
                        if (m != null) addPlans(idx, m.match(a.owner, a.name, a.descriptor))
                    }
                    is InsnAction.TypeInsn -> if (a.opcode == Opcodes.NEW) {
                        addPlans(idx, newMatcher.match(a.type))
                    }
                    is InsnAction.Insn -> addPlans(idx, terminalMatcher.matchInsn(a.opcode))
                    else -> Unit
                }
            }

            if (opcodeSeqEntries.isNotEmpty()) {
                val indexedViews = buildRealInsnViews(actions)
                val seqEvents = opcodeSeqMatcher.match(indexedViews.views)
                for (ev in seqEvents) {
                    val idx = indexedViews.indices.getOrNull(ev.anchorIndex) ?: continue
                    indexedPlans += IndexedPlan(idx, planner.plan(ev.copy(anchorIndex = idx)))
                }
            }

            val headEvents = terminalMatcher.matchHead()
            val headInsertIdx = firstRealInsnIndex(actions)

            // 倒序插入避免索引漂移
            val sortedPlans = indexedPlans.sortedByDescending { it.index }
            for (ip in sortedPlans) {
                applyPlan(replayer, ip.index, ip.plan, actions)
            }
            if (headEvents.isNotEmpty() && headInsertIdx >= 0) {
                for (ev in headEvents.reversed()) {
                    val emission = toEmission(ev.siteSpec, isVoid = true)
                    replayer.insertBefore(headInsertIdx, emission)
                }
            }

            replayer.replay(downstream)
        }

        /**
         * 按策略把 [InsnAction.InsertEmission] 写入 replayer。
         *  - IMMEDIATE：原位（site.shift 决定 before/after，offset == 0）
         *  - DEFERRED：锚点 + offset 条真实指令后
         *  - BUFFERING：锚点 - offset 条真实指令前（OpcodeSeq 命中也走这条）
         */
        private fun applyPlan(
            replayer: Replayer,
            anchorIdx: Int,
            plan: EmissionPlan,
            actions: List<InsnAction>,
        ) {
            val site = plan.site
            val absOffset = kotlin.math.abs(site.offset)
            val isVoid = isAnchorVoid(actions.getOrNull(anchorIdx))
            val emission = toEmission(site, isVoid)
            when (plan.strategy) {
                EmissionPlan.Strategy.IMMEDIATE -> insertAtAnchor(replayer, anchorIdx, site, emission)
                EmissionPlan.Strategy.DEFERRED -> {
                    val target = skipForward(actions, anchorIdx, absOffset)
                    replayer.insertAfter(target, emission)
                }
                EmissionPlan.Strategy.BUFFERING -> {
                    val target = skipBackward(actions, anchorIdx, absOffset)
                    replayer.insertBefore(target, emission)
                }
            }
        }

        private fun insertAtAnchor(replayer: Replayer, anchorIdx: Int, site: SiteSpec, emission: InsnAction) {
            when (site.kind) {
                AdviceKind.GRAFT -> if (site.shift == Shift.AFTER) {
                    replayer.insertAfter(anchorIdx, emission)
                } else {
                    replayer.insertBefore(anchorIdx, emission)
                }
                AdviceKind.TRIM -> replayer.insertAfter(anchorIdx, emission)
                else -> replayer.insertBefore(anchorIdx, emission)
            }
        }

        private fun toEmission(site: SiteSpec, isVoid: Boolean): InsnAction.InsertEmission =
            InsnAction.InsertEmission(site.kind, site, isVoid)

        private fun isAnchorVoid(a: InsnAction?): Boolean = when (a) {
            is InsnAction.MethodInsn -> a.descriptor.endsWith(")V")
            is InsnAction.FieldInsn -> a.opcode == Opcodes.PUTFIELD || a.opcode == Opcodes.PUTSTATIC
            is InsnAction.Insn -> a.opcode == Opcodes.ATHROW || a.opcode in Opcodes.IRETURN..Opcodes.RETURN
            else -> false
        }

        private fun skipForward(actions: List<InsnAction>, from: Int, n: Int): Int {
            if (n <= 0) return from
            var i = from
            var left = n
            while (i + 1 < actions.size && left > 0) {
                i++
                if (isRealInsn(actions[i])) left--
            }
            return i
        }

        private fun skipBackward(actions: List<InsnAction>, from: Int, n: Int): Int {
            if (n <= 0) return from
            var i = from
            var left = n
            while (i > 0 && left > 0) {
                i--
                if (isRealInsn(actions[i])) left--
            }
            return i
        }

        private fun firstRealInsnIndex(actions: List<InsnAction>): Int {
            for ((i, a) in actions.withIndex()) if (isRealInsn(a)) return i
            return -1
        }

        private fun isRealInsn(a: InsnAction): Boolean = when (a) {
            is InsnAction.Insn, is InsnAction.IntInsn, is InsnAction.VarInsn,
            is InsnAction.TypeInsn, is InsnAction.FieldInsn, is InsnAction.MethodInsn,
            is InsnAction.InvokeDynamicInsn, is InsnAction.JumpInsn, is InsnAction.LdcInsn,
            is InsnAction.IincInsn, is InsnAction.TableSwitchInsn, is InsnAction.LookupSwitchInsn,
            is InsnAction.MultiANewArrayInsn -> true
            else -> false
        }

        private data class IndexedActionViews(
            val indices: List<Int>,
            val views: List<OpcodeSeqMatcher.InsnView>,
        )

        private fun buildRealInsnViews(actions: List<InsnAction>): IndexedActionViews {
            val indices = ArrayList<Int>(actions.size)
            val views = ArrayList<OpcodeSeqMatcher.InsnView>(actions.size)
            for ((index, action) in actions.withIndex()) {
                val view = toInsnView(action) ?: continue
                indices += index
                views += view
            }
            return IndexedActionViews(indices, views)
        }

        private fun toInsnView(a: InsnAction): OpcodeSeqMatcher.InsnView? {
            return when (a) {
                is InsnAction.Insn -> OpcodeSeqMatcher.InsnView(a.opcode)
                is InsnAction.IntInsn -> OpcodeSeqMatcher.InsnView(a.opcode)
                is InsnAction.VarInsn -> OpcodeSeqMatcher.InsnView(a.opcode)
                is InsnAction.TypeInsn -> OpcodeSeqMatcher.InsnView(a.opcode, owner = a.type)
                is InsnAction.FieldInsn -> OpcodeSeqMatcher.InsnView(a.opcode, a.owner, a.name, a.descriptor)
                is InsnAction.MethodInsn -> OpcodeSeqMatcher.InsnView(a.opcode, a.owner, a.name, a.descriptor)
                is InsnAction.LdcInsn -> OpcodeSeqMatcher.InsnView(Opcodes.LDC, cst = a.cst)
                is InsnAction.JumpInsn -> OpcodeSeqMatcher.InsnView(a.opcode)
                is InsnAction.IincInsn -> OpcodeSeqMatcher.InsnView(Opcodes.IINC)
                is InsnAction.TableSwitchInsn -> OpcodeSeqMatcher.InsnView(Opcodes.TABLESWITCH)
                is InsnAction.LookupSwitchInsn -> OpcodeSeqMatcher.InsnView(Opcodes.LOOKUPSWITCH)
                is InsnAction.MultiANewArrayInsn -> OpcodeSeqMatcher.InsnView(Opcodes.MULTIANEWARRAY, owner = a.descriptor)
                is InsnAction.InvokeDynamicInsn -> OpcodeSeqMatcher.InsnView(Opcodes.INVOKEDYNAMIC, name = a.name, descriptor = a.descriptor)
                else -> null
            }
        }
    }
}
