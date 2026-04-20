package taboolib.module.incision.weaver

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.ClassRemapper
import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.cache.IncisionCache
import taboolib.module.incision.diagnostic.Forensics
import taboolib.module.incision.diagnostic.Trauma
import taboolib.module.incision.loader.JvmtiBackend
import taboolib.module.incision.remap.NameResolver
import taboolib.module.incision.remap.RemapRouter
import taboolib.module.incision.runtime.AdviceKind
import taboolib.module.incision.weaver.site.SiteSpec
import java.io.File

/**
 * 主 weaver — 接收一个目标类的字节码，针对若干 target 方法注入 dispatcher 调用。
 *
 * 注入的 JVM 调用签名（固定由 IncisionGate / TheatreDispatcher 持有）：
 * ```
 * INVOKESTATIC taboolib/module/incision/runtime/TheatreDispatcher.dispatch
 *   (Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
 * ```
 * 后续 GateBootstrapper 启用后会把调用重定向到 `taboolib/incision/gate/IncisionGate`。
 *
 * 当前支持 Lead / Trail / Splice / Excise；Graft / Bypass / Trim 标记为待实现。
 */
class Scalpel(
    private val resolver: NameResolver = RemapRouter,
    private val targetsByOwner: Map<String, List<AdviceTargetSpec>>,
) {

    data class AdviceTargetSpec(
        val target: MethodCoordinate,
        val kinds: Set<AdviceKind>,
        val sites: List<SiteSpec> = emptyList(),
    )

    fun weave(originalBytes: ByteArray): ByteArray {
        return try {
            val probeReader = ClassReader(originalBytes)
            val probeOwner = probeReader.className
            val sourceBytes = JvmtiBackend.getCachedOriginal(probeOwner) ?: run {
                JvmtiBackend.cacheOriginal(probeOwner, originalBytes)
                originalBytes
            }
            val reader = ClassReader(sourceBytes)
            val owner = reader.className
            val targets = targetsByOwner[owner] ?: return originalBytes
            val loader = currentTransformLoader.get()
            val writer = SafeClassWriter(reader, loader)
            val visitor = WeavingClassVisitor(Opcodes.ASM9, writer, targets)
            val pipeline = if (resolver.isRemappableTarget(owner)) {
                ClassRemapper(visitor, RemapperBridge(resolver))
            } else visitor
            reader.accept(pipeline, ClassReader.EXPAND_FRAMES)
            var bytes = writer.toByteArray()
            // 二次 pass — 走 SiteWeaver 处理 GRAFT / BYPASS / TRIM 站点
            val anySites = targets.any { it.sites.isNotEmpty() }
            if (anySites) {
                bytes = applySiteWeaver(bytes, targets, loader)
            }
            generateAndRegisterBodies(owner, sourceBytes, targets)
            devDumpIfEnabled(owner, sourceBytes, bytes)
            bytes
        } catch (t: Throwable) {
            throw Trauma.Weaving.AsmVerifyError(
                "<batch>", MethodCoordinate("?", "?", "?"),
                verifyOutput = t.stackTraceToString(), cause = t
            )
        }
    }

    companion object {
        /**
         * 由 [InstrumentationBackend] 在 transform 回调中设置，携带目标类的 ClassLoader。
         * [SafeClassWriter.getClassLoader] 读取此值，让 getCommonSuperClass 的 Class.forName
         * 能看见 fixture / 插件 ClassLoader 上的类型，避免帧合并回退 Object 引发 VerifyError。
         */
        val currentTransformLoader: ThreadLocal<ClassLoader?> = ThreadLocal.withInitial { null }
    }

    private fun configDigest(targets: List<AdviceTargetSpec>): String = buildString {
        for (t in targets) {
            append(t.target.owner).append('#').append(t.target.name).append(t.target.descriptor)
            append('|').append(t.kinds.joinToString(",") { it.name })
            for (s in t.sites) {
                append("|site:").append(s.anchor).append(':').append(s.kind).append(':')
                    .append(s.ownerPattern).append('.').append(s.namePattern).append(s.descPattern)
                    .append('@').append(s.shift).append('#').append(s.ordinal)
            }
            append(';')
        }
    }

    private fun devDumpIfEnabled(owner: String, before: ByteArray, after: ByteArray) {
        if (System.getProperty("incision.dev", "false").toBoolean().not()) return
        val dir = File(System.getProperty("incision.devDir", ".dev/incision"))
        IncisionCache.dump(dir, owner.replace('/', '.'), before, after)
    }

    /**
     * 第二 pass — 站点织入。采用 Tree API 流水：
     *
     *  1. `ClassReader.accept(ClassNode, EXPAND_FRAMES)` 读入完整树
     *  2. 对每个需要织入的 method，用 [SiteWeaver.wrapMethod] 包装成 [TreeMethodDriver]，
     *     然后 `method.accept(driver)` 重放原方法进 driver；driver.visitEnd 完成 instructions 变异并发射到新 MethodNode
     *  3. 变异后的方法替换 ClassNode.methods 里对应项
     *  4. [FrameVerifier] 用 `Analyzer<BasicValue>` 跑一遍做帧一致性预检；若失败，回退原字节码
     *  5. `ClassNode.accept(ClassWriter(COMPUTE_FRAMES))` 写出
     *
     * 回退策略：Forensics.warn 记录失败方法与原因，返回 `bytes` 不动。保守避免把坏 class 喂给 JVM 触发 VerifyError。
     */
    private fun applySiteWeaver(bytes: ByteArray, targets: List<AdviceTargetSpec>, loader: ClassLoader?): ByteArray {
        val reader = ClassReader(bytes)
        val original = org.objectweb.asm.tree.ClassNode()
        reader.accept(original, ClassReader.EXPAND_FRAMES)

        val weaved = org.objectweb.asm.tree.ClassNode()
        // 拷贝类级元数据到新 ClassNode；方法清单按需替换
        weaved.visit(
            original.version, original.access, original.name, original.signature,
            original.superName, original.interfaces.toTypedArray()
        )
        weaved.sourceFile = original.sourceFile
        weaved.sourceDebug = original.sourceDebug
        weaved.outerClass = original.outerClass
        weaved.outerMethod = original.outerMethod
        weaved.outerMethodDesc = original.outerMethodDesc
        weaved.innerClasses = original.innerClasses
        weaved.nestHostClass = original.nestHostClass
        weaved.nestMembers = original.nestMembers
        weaved.permittedSubclasses = original.permittedSubclasses
        weaved.visibleAnnotations = original.visibleAnnotations
        weaved.invisibleAnnotations = original.invisibleAnnotations
        weaved.visibleTypeAnnotations = original.visibleTypeAnnotations
        weaved.invisibleTypeAnnotations = original.invisibleTypeAnnotations
        weaved.attrs = original.attrs
        weaved.fields = original.fields

        for (method in original.methods) {
            val sites = targets
                .filter { it.target.name == method.name && matchesDesc(it.target.descriptor, method.desc) }
                .flatMap { it.sites }
            if (sites.isNullOrEmpty()) {
                weaved.methods.add(method)
                continue
            }
            // 把原方法 replay 给新的 MethodNode（driver 是 MethodNode），visitEnd 触发 Tree 变异
            val rewritten = org.objectweb.asm.tree.MethodNode(
                Opcodes.ASM9, method.access, method.name, method.desc, method.signature,
                method.exceptions?.toTypedArray()
            )
            val driver = SiteWeaver(sites).wrapMethod(
                Opcodes.ASM9, rewritten,
                method.access, method.name, method.desc, method.signature,
                method.exceptions?.toTypedArray()
            )
            method.accept(driver)
            weaved.methods.add(rewritten)
        }

        // 先让 ClassWriter 以 COMPUTE_MAXS | COMPUTE_FRAMES 产出字节，
        // 再用 ClassReader 重读出来——此时 maxStack / maxLocals / frames 全部已由 ASM 重算，
        // FrameVerifier 校验的是最终产物的真实状态，而不是 MethodNode 残留的旧 maxStack。
        // 先跑一遍 FrameVerifier（Analyzer<BasicValue>）做方法级预检，把 ClassWriter COMPUTE_FRAMES
        // 一旦挂会整个 class 回退的行为，降维成"哪个方法坏了哪个方法回退"。这里只拿诊断信息，不阻塞写出。
        val preReport = FrameVerifier.verify(weaved)
        if (!preReport.ok) {
            preReport.failures.forEach { f ->
                Forensics.error(
                    "SiteWeaver 写出前 FrameVerifier 预检失败 — ${original.name}.${f.method}${f.descriptor} — ${f.message}",
                    f.cause,
                )
            }
        }

        val writer = SafeClassWriter(reader, loader)
        val outBytes = try {
            weaved.accept(writer)
            writer.toByteArray()
        } catch (t: Throwable) {
            Forensics.error(
                "SiteWeaver ClassWriter 写出失败 — owner=${original.name} — ${t.javaClass.name}: ${t.message}；尝试逐方法定位",
                t,
            )
            // 逐方法写，定位到具体哪个方法触发 Frame.merge AIOOB
            for (m in weaved.methods) {
                val probeNode = org.objectweb.asm.tree.ClassNode().also {
                    it.version = weaved.version
                    it.access = weaved.access
                    it.name = weaved.name
                    it.superName = weaved.superName
                    it.interfaces = weaved.interfaces
                }
                probeNode.methods.add(m)
                try {
                    probeNode.accept(SafeClassWriter(reader, loader))
                } catch (mt: Throwable) {
                    Forensics.error(
                        "坏方法定位：${original.name}.${m.name}${m.desc} — ${mt.javaClass.name}: ${mt.message}",
                        mt,
                    )
                }
                probeNode.methods.clear()
            }
            return bytes
        }

        val verified = org.objectweb.asm.tree.ClassNode()
        org.objectweb.asm.ClassReader(outBytes).accept(verified, 0)
        val report = FrameVerifier.verify(verified)
        if (!report.ok) {
            Forensics.warn(
                "SiteWeaver FrameVerifier 失败 — owner=${original.name} ${report.summary()}；回退原字节码"
            )
            report.failures.forEach { f ->
                Forensics.error("FrameVerifier failure: ${original.name}.${f.method}${f.descriptor} — ${f.message}", f.cause)
            }
            return bytes
        }

        return outBytes
    }

    private val bodyKinds = setOf(AdviceKind.SPLICE, AdviceKind.EXCISE, AdviceKind.BYPASS)

    private fun generateAndRegisterBodies(
        owner: String,
        originalBytes: ByteArray,
        targets: List<AdviceTargetSpec>,
    ) {
        val spliceMethods: Set<Pair<String, String>> = targets
            .filter { it.kinds.any { k -> k in bodyKinds } }
            .map { it.target.name to it.target.descriptor }
            .toSet()
        if (spliceMethods.isEmpty()) return
        val bodiesName = BridgeClassLoader.bodiesClassName(owner)
        if (BridgeClassLoader.INSTANCE.hasBodies(bodiesName)) return
        val sourceBytes = try {
            JvmtiBackend.getCachedOriginal(owner)
        } catch (_: Throwable) { null } ?: run {
            try {
                JvmtiBackend.cacheOriginal(owner, originalBytes)
            } catch (t: Throwable) {
                Forensics.error("JvmtiBackend.cacheOriginal failed: $owner — ${t.message}", t)
            }
            originalBytes
        }
        val bodiesBytes = try {
            BodiesClassGenerator.generate(sourceBytes, owner, spliceMethods)
        } catch (t: Throwable) {
            Forensics.error("BodiesClassGenerator.generate failed: $owner — ${t.message}", t)
            null
        }
        if (bodiesBytes == null) return
        try {
            BridgeClassLoader.INSTANCE.defineBodies(bodiesName, bodiesBytes)
            // 注册能解析目标类的 ClassLoader 为 delegate
            // contextClassLoader 不一定是插件 CL，所以多注册几个来源
            Thread.currentThread().contextClassLoader?.let {
                BridgeClassLoader.INSTANCE.registerDelegate(it)
            }
            Scalpel::class.java.classLoader?.let {
                BridgeClassLoader.INSTANCE.registerDelegate(it)
            }
            Forensics.debug("Bodies class defined: $bodiesName")
        } catch (t: Throwable) {
            Forensics.error("Bodies class define failed: $bodiesName — ${t.message}", t)
        }
    }

    private fun matchesDesc(pattern: String, actual: String): Boolean {
        if (pattern == actual) return true
        if (pattern == "(*)" || pattern == "(*)V" || pattern == "()*") return true
        return false
    }

    private class RemapperBridge(val r: NameResolver) : org.objectweb.asm.commons.Remapper() {
        override fun map(internalName: String): String = r.resolveOwner(internalName)
        override fun mapMethodName(owner: String, name: String, descriptor: String): String =
            r.resolveMethod(owner, name, descriptor).first
        override fun mapFieldName(owner: String, name: String, descriptor: String): String =
            r.resolveField(owner, name, descriptor).first
    }

    private class WeavingClassVisitor(
        api: Int, cv: ClassWriter,
        val targets: List<AdviceTargetSpec>,
    ) : org.objectweb.asm.ClassVisitor(api, cv) {

        private lateinit var className: String
        override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
            className = name
            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            val base = super.visitMethod(access, name, descriptor, signature, exceptions)
            val matching = targets.filter { matchesDescriptor(it.target.descriptor, descriptor) && it.target.name == name }
            if (matching.isEmpty()) return base
            val mergedKinds = mutableSetOf<AdviceKind>()
            for (s in matching) mergedKinds.addAll(s.kinds)
            val mergedSpec = AdviceTargetSpec(matching.first().target, mergedKinds, matching.flatMap { it.sites })
            Forensics.debug("weave inject $className.$name$descriptor kinds=$mergedKinds static=${(access and Opcodes.ACC_STATIC) != 0}")
            return AdviceInjector(api, base, access, name, descriptor, className, mergedSpec)
        }

        private fun matchesDescriptor(pattern: String, actual: String): Boolean {
            if (pattern == actual) return true
            if (pattern == "(*)" || pattern == "(*)V" || pattern == "()*") return true
            // 通配实参或返回值
            val pOpen = pattern.indexOf('('); val pClose = pattern.indexOf(')')
            val aOpen = actual.indexOf('('); val aClose = actual.indexOf(')')
            if (pOpen < 0 || pClose < 0 || aOpen < 0 || aClose < 0) return false
            val pArgs = pattern.substring(pOpen + 1, pClose)
            val pRet = pattern.substring(pClose + 1)
            val aArgs = actual.substring(aOpen + 1, aClose)
            val aRet = actual.substring(aClose + 1)
            val argsOk = pArgs == "*" || pArgs == aArgs
            val retOk = pRet == "*" || pRet == aRet
            return argsOk && retOk
        }

    }

    /**
     * 为单个方法注入 advice 调用。
     *
     * - **Lead**: onMethodEnter 处调用 dispatcher，丢弃返回值
     * - **Trail**: onMethodExit（非异常）处调用 dispatcher，丢弃返回值
     * - **Splice**: onMethodEnter 处调用 dispatcher；若返回值非 null 则按方法返回类型转换并直接 return
     * - **Excise**: 整段替换 — 在 onMethodEnter 处调用 dispatcher 并直接 return（忽略原方法体的剩余指令；ASM AdviceAdapter 不能"删除剩余指令"，因此采用：dispatcher 返回 null 时回退原方法）
     * - **Graft / Bypass / Trim**: 需要 @Site 锚点扫描，本 weaver 暂不实现，运行时由 dispatcher 链兜底（仍触发 handler，但不能改写 INVOKE / FIELD_GET 等指令位置）
     */
    private class AdviceInjector(
        api: Int, mv: MethodVisitor, access: Int,
        val methodName: String, val methodDesc: String,
        val ownerName: String,
        val spec: AdviceTargetSpec,
    ) : AdviceAdapter(api, mv, access, methodName, methodDesc) {

        // 使用用户声明的 target.signature 作为 dispatch key，
        // 保持与 TheatreDispatcher.chains 的 key 一致（remap 前的名字）
        private val targetSig = spec.target.signature
        private val returnType: org.objectweb.asm.Type = org.objectweb.asm.Type.getReturnType(methodDesc)
        private val kindsWithSite = spec.sites.map { it.kind }.toSet()
        private val entryKinds = spec.kinds - kindsWithSite

        override fun onMethodEnter() {
            if (AdviceKind.LEAD in entryKinds) {
                emitDispatcherCall("@LEAD")
                pop()
            }
            // 入口 @SPLICE 只对"无 site 的 SPLICE/EXCISE/BYPASS/GRAFT/TRIM"开。GRAFT/BYPASS/TRIM
            // 一旦带 site，就由 SiteWeaver 在锚点位置发射 dispatcher——再在入口插一发会让 handler 触发
            // 2 次（曾观察到 surgeon-graft-field-get expected=1 actual=2、surgeon-bypass-invoke
            // expected=2000 actual=1000 等），因为 TheatreDispatcher.matchesPhase 把
            // SPLICE/EXCISE/BYPASS/GRAFT/TRIM 都映到 @SPLICE 相位。
            val hasSplicePhase = entryKinds.any {
                it == AdviceKind.SPLICE || it == AdviceKind.EXCISE
                    || it == AdviceKind.BYPASS || it == AdviceKind.GRAFT || it == AdviceKind.TRIM
            }
            if (hasSplicePhase) {
                emitDispatcherCall("@SPLICE")
                emitReturnIfNonNull()
            }
        }

        override fun onMethodExit(opcode: Int) {
            if (AdviceKind.TRAIL !in entryKinds) return
            if (opcode == ATHROW) {
                emitThrowTrailCall()
            } else {
                emitDispatcherCall("@TRAIL")
                pop()
            }
        }

        /**
         * 异常出口（ATHROW）的 TRAIL 调度：
         *   栈前 [thr] → 栈后 [thr]
         *   把 throwable 作为 args[0] 传入 dispatch，phase=@TRAIL_THROW；
         *   dispatcher 侧按 entry.onThrow 过滤并回填 Theatre.throwable。
         */
        private fun emitThrowTrailCall() {
            val thrLocal = newLocal(org.objectweb.asm.Type.getType("Ljava/lang/Throwable;"))
            visitVarInsn(Opcodes.ASTORE, thrLocal)
            visitLdcInsn("$targetSig@TRAIL_THROW")
            if ((methodAccess and Opcodes.ACC_STATIC) != 0) {
                visitInsn(Opcodes.ACONST_NULL)
            } else {
                loadThis()
            }
            visitInsn(Opcodes.ICONST_1)
            visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")
            visitInsn(Opcodes.DUP)
            visitInsn(Opcodes.ICONST_0)
            visitVarInsn(Opcodes.ALOAD, thrLocal)
            visitInsn(Opcodes.AASTORE)
            visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "io/izzel/incision/bridge/IncisionBridge",
                "dispatch",
                "(Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                false,
            )
            visitInsn(Opcodes.POP)
            visitVarInsn(Opcodes.ALOAD, thrLocal)
        }

        /**
         * stack: (Object) — 若 != null，按 returnType 拆箱并 return；否则 pop 继续原方法体
         */
        private fun emitReturnIfNonNull() {
            val skipLabel = newLabel()
            // dup; ifnull skip
            visitInsn(Opcodes.DUP)
            visitJumpInsn(Opcodes.IFNULL, skipLabel)
            // 非 null，按返回类型拆箱并 return
            when (returnType.sort) {
                org.objectweb.asm.Type.VOID -> {
                    pop()
                    visitInsn(Opcodes.RETURN)
                }
                org.objectweb.asm.Type.BOOLEAN -> {
                    unboxAndReturn("java/lang/Boolean", "booleanValue", "()Z", Opcodes.IRETURN)
                }
                org.objectweb.asm.Type.BYTE -> {
                    unboxAndReturn("java/lang/Byte", "byteValue", "()B", Opcodes.IRETURN)
                }
                org.objectweb.asm.Type.CHAR -> {
                    unboxAndReturn("java/lang/Character", "charValue", "()C", Opcodes.IRETURN)
                }
                org.objectweb.asm.Type.SHORT -> {
                    unboxAndReturn("java/lang/Short", "shortValue", "()S", Opcodes.IRETURN)
                }
                org.objectweb.asm.Type.INT -> {
                    unboxAndReturn("java/lang/Integer", "intValue", "()I", Opcodes.IRETURN)
                }
                org.objectweb.asm.Type.LONG -> {
                    unboxAndReturn("java/lang/Long", "longValue", "()J", Opcodes.LRETURN)
                }
                org.objectweb.asm.Type.FLOAT -> {
                    unboxAndReturn("java/lang/Float", "floatValue", "()F", Opcodes.FRETURN)
                }
                org.objectweb.asm.Type.DOUBLE -> {
                    unboxAndReturn("java/lang/Double", "doubleValue", "()D", Opcodes.DRETURN)
                }
                else -> {
                    visitTypeInsn(Opcodes.CHECKCAST, returnType.internalName)
                    visitInsn(Opcodes.ARETURN)
                }
            }
            visitLabel(skipLabel)
            // 走 null 分支：栈顶仍有 null，pop 掉
            visitInsn(Opcodes.POP)
        }

        private fun unboxAndReturn(boxedInternal: String, method: String, desc: String, retOp: Int) {
            visitTypeInsn(Opcodes.CHECKCAST, boxedInternal)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, boxedInternal, method, desc, false)
            visitInsn(retOp)
        }

        private fun emitDispatcherCall(phaseSuffix: String = "") {
            visitLdcInsn("$targetSig$phaseSuffix")
            if ((methodAccess and Opcodes.ACC_STATIC) != 0) {
                visitInsn(Opcodes.ACONST_NULL)
            } else {
                loadThis()
            }
            loadArgArray()
            // !! 必须使用非 taboolib.* 的包名，否则会被 taboolib-gradle-plugin
            //    的 RelocateRemapper 在每个插件打包时重定向为 <group>.taboolib.*，
            //    导致跨插件无法共享同一桥。
            visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "io/izzel/incision/bridge/IncisionBridge",
                "dispatch",
                "(Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                false,
            )
        }
    }

    /**
     * JVMTI 安全的 ClassWriter — 重写 getCommonSuperClass / getClassLoader 以避免跨 ClassLoader 加载失败。
     *
     * 默认实现使用 Class.forName() 并以 ClassWriter 自己的 ClassLoader 为起点；在 JVMTI retransform
     * 场景下目标类的依赖（如插件 fixture）可能不在 ASM 所在 ClassLoader 中，导致 TypeNotPresentException
     * → 帧计算失败 → JVM 静默拒绝 retransformed 字节码（甚至把 this slot 降格为 Object 造成 VerifyError）。
     *
     * 修复：
     *  1. type1==type2 短路，Object 参与合并直接返回 Object —— 覆盖 50%+ 常见场景无需 Class.forName。
     *  2. [getClassLoader] 优先返回 [targetLoader]（由 transformer 传入，等于目标类的定义 CL），
     *     Class.forName 即可看见 fixture / 插件类型，彻底消除 frame 回退 Object 的诱因。
     *  3. 任何失败兜底 Object，避免 weave 整体崩盘。
     */
    private class SafeClassWriter(reader: ClassReader, private val targetLoader: ClassLoader?) :
        ClassWriter(reader, COMPUTE_MAXS or COMPUTE_FRAMES) {

        override fun getClassLoader(): ClassLoader =
            targetLoader ?: Thread.currentThread().contextClassLoader ?: SafeClassWriter::class.java.classLoader

        override fun getCommonSuperClass(type1: String, type2: String): String {
            if (type1 == type2) return type1
            if (type1 == "java/lang/Object" || type2 == "java/lang/Object") return "java/lang/Object"
            return try {
                super.getCommonSuperClass(type1, type2)
            } catch (_: Throwable) {
                "java/lang/Object"
            }
        }
    }
}
