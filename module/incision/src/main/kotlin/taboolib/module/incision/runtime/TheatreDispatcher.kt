package taboolib.module.incision.runtime

import io.izzel.incision.bridge.IncisionBridge
import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.api.Resume
import taboolib.module.incision.api.Theatre
import taboolib.module.incision.diagnostic.Forensics
import taboolib.module.incision.diagnostic.Trauma
import taboolib.module.incision.pred.EvalContextImpl
import taboolib.module.incision.weaver.BodiesClassGenerator
import taboolib.module.incision.weaver.BridgeClassLoader
import java.util.concurrent.ConcurrentHashMap

/**
 * 手术现场调度器 — 接收织入字节码的回调，按 advice 链顺序触发 handler。
 *
 * 由于实际部署时调度入口是 IncisionGate（系统 ClassLoader 持有），
 * 本类是"本地实现"，会被网关在最终代理时复用 / 包装。
 */
object TheatreDispatcher {

    @JvmField
    val BYPASS_MISS: Any = IncisionBridge.bypassMiss()

    private val chains = ConcurrentHashMap<String, AdviceChain>()
    private val wildcardEntries = ConcurrentHashMap<String, MutableList<AdviceEntry>>()

    /** -Dincision.predicate.strict=true 时，谓词运行期异常上抛而非吞掉。 */
    private val predicateStrict: Boolean =
        System.getProperty("incision.predicate.strict", "false").toBoolean()

    /** 谓词异常 warn 去重表：advice id → 已 warn 标记。 */
    private val predicateWarnedIds = ConcurrentHashMap<String, Boolean>()

    private inline fun warnOnce(id: String, block: () -> Unit) {
        if (predicateWarnedIds.putIfAbsent(id, true) == null) block()
    }

    // bodies 方法缓存：baseSig → Method（未命中时缓存为 null 的 Optional 包装）
    private val bodyInvokerCache = ConcurrentHashMap<String, java.util.Optional<java.lang.reflect.Method>>()

    /**
     * 从 BridgeClassLoader 中查找 bodies 类的对应方法。
     *
     * @param baseSig 格式如 "com/example/Foo.greet(Ljava/lang/String;I)I"
     * @return bodies 类中的 static Method，或 null
     */
    fun resolveBodyInvoker(baseSig: String): java.lang.reflect.Method? {
        val cached = bodyInvokerCache[baseSig]
        if (cached != null) return cached.orElse(null)
        val resolved: java.lang.reflect.Method? = try {
            val parsed = parseOwnerAndMethod(baseSig)
            if (parsed == null) {
                null
            } else {
                val bodiesClassName = BridgeClassLoader.bodiesClassName(parsed.ownerInternal)
                if (!BridgeClassLoader.INSTANCE.hasBodies(bodiesClassName)) {
                    null
                } else {
                    val bodiesClass = BridgeClassLoader.INSTANCE.loadClass(bodiesClassName)
                    val bodyMethodName = parsed.methodName + BodiesClassGenerator.BODY_METHOD_SUFFIX
                    bodiesClass.getMethod(bodyMethodName, Any::class.java, Array<Any>::class.java)
                }
            }
        } catch (t: Throwable) {
            Forensics.debug("resolveBodyInvoker 未命中: $baseSig — ${t.message}")
            null
        }
        bodyInvokerCache[baseSig] = java.util.Optional.ofNullable(resolved)
        return resolved
    }

    private data class ParsedSig(val ownerInternal: String, val methodName: String, val desc: String)

    /**
     * 从 baseSig 解析 owner/name/desc。
     * 格式：
     *   "com/example/Foo.greet(Ljava/lang/String;I)I"
     */
    private fun parseOwnerAndMethod(baseSig: String): ParsedSig? {
        val dotIdx = baseSig.indexOf('.')
        if (dotIdx < 0) return null
        val owner = baseSig.substring(0, dotIdx)
        val rest = baseSig.substring(dotIdx + 1)
        val parenIdx = rest.indexOf('(')
        if (parenIdx < 0) return null
        val name = rest.substring(0, parenIdx)
        val desc = rest.substring(parenIdx)
        return ParsedSig(owner, name, desc)
    }

    fun chainOf(target: MethodCoordinate): AdviceChain =
        chains.computeIfAbsent(target.signature) { AdviceChain(target) }

    fun register(entry: AdviceEntry) {
        val desc = entry.target.descriptor
        if (desc.contains('*')) {
            val ownerName = "${entry.target.owner}.${entry.target.name}"
            wildcardEntries.computeIfAbsent(ownerName) { java.util.concurrent.CopyOnWriteArrayList() }.add(entry)
            // 也在 chains 中创建通配签名对应的 chain，保证 weaver 用通配签名 LDC 时能命中
            chainOf(entry.target).add(entry)
            for ((sig, chain) in chains) {
                if (sig == entry.target.signature) continue
                if (chain.target.owner == entry.target.owner && chain.target.name == entry.target.name) {
                    chain.add(entry)
                }
            }
        } else {
            val chain = chainOf(entry.target)
            chain.add(entry)
            val ownerName = "${entry.target.owner}.${entry.target.name}"
            wildcardEntries[ownerName]?.forEach { w -> chain.add(w) }
        }
    }

    fun unregister(target: MethodCoordinate, id: String): Boolean =
        chains[target.signature]?.remove(id) ?: false

    /**
     * 织入字节码的回调入口。返回值用作目标方法的返回值（若被覆盖）。
     *
     * @param targetSig 编译期常量字符串，格式 `owner.name(desc)return`
     * @param self      实例（静态方法时为 null）
     * @param args      原方法实参
     * @param originalInvoker 调用原方法的桥（由 weaver 注入；splice 链尾会用到）
     */
    @JvmStatic
    fun dispatch(
        targetSig: String,
        self: Any?,
        args: Array<Any?>,
        originalInvoker: ((Array<Any?>) -> Any?)? = null,
    ): Any? {
        if (taboolib.module.incision.reflex.IncisionReflex.isCurrentlySuppressed()) {
            return originalInvoker?.invoke(args)
        }
        val parsedSig = DispatchSignatureCodec.parse(targetSig)
        val baseSig = parsedSig.baseSig
        val adviceId = parsedSig.adviceId
        val phase = parsedSig.phase
        val chain = chains[baseSig]
        if (chain == null) return originalInvoker?.invoke(args)
        val isThrowPhase = phase == "TRAIL_THROW"
        val entries = chain.list().filter { e ->
            if (!e.enabled) return@filter false
            if (adviceId != null) return@filter e.id == adviceId
            if (phase == null) return@filter true
            if (isThrowPhase) return@filter e.kind == AdviceKind.TRAIL && e.onThrow
            matchesPhase(e, phase)
        }
        if (entries.isEmpty()) return originalInvoker?.invoke(args)

        // SPLICE 相位下若 weaver 未注入 invoker，尝试从 bodies side-car 解析
        val effectiveInvoker: ((Array<Any?>) -> Any?)? = originalInvoker ?: run {
            if (phase == "SPLICE") {
                // 确保 BridgeClassLoader 能解析目标类：用 self 的 CL 注册为 delegate
                self?.javaClass?.classLoader?.let { BridgeClassLoader.INSTANCE.registerDelegate(it) }
                val bodyMethod = resolveBodyInvoker(baseSig)
                if (bodyMethod != null) {
                    { callArgs -> bodyMethod.invoke(null, self, callArgs) }
                } else null
            } else null
        }

        val target = chain.target
        // TRAIL_THROW 相位：args[0] 是织入期传入的 Throwable；ctx.throwable 回填
        val (ctxArgs, pendingThrow) = if (isThrowPhase) {
            val thr = args.firstOrNull() as? Throwable
            emptyArray<Any?>() to thr
        } else args to null
        val ctx = TheatreImpl(target, self, ctxArgs, entries, effectiveInvoker)
        if (pendingThrow != null) ctx.setPendingThrowable(pendingThrow)
        return ctx.runChain()
    }

    @JvmStatic
    fun dispatchBypass(targetSig: String, self: Any?, args: Array<Any?>): Any? {
        if (taboolib.module.incision.reflex.IncisionReflex.isCurrentlySuppressed()) {
            return BYPASS_MISS
        }
        val parsedSig = DispatchSignatureCodec.parse(targetSig)
        val baseSig = parsedSig.baseSig
        val adviceId = parsedSig.adviceId
        val phase = parsedSig.phase
        val chain = chains[baseSig] ?: return BYPASS_MISS
        val entries = chain.list().filter { e ->
            if (!e.enabled) return@filter false
            if (adviceId != null) return@filter e.id == adviceId
            if (phase == null) return@filter e.kind == AdviceKind.BYPASS
            matchesPhase(e, phase)
        }
        if (entries.isEmpty()) return BYPASS_MISS
        val ctx = TheatreImpl(chain.target, self, args, entries, null)
        val result = ctx.runChain()
        return if (ctx.wasIntercepted()) result else BYPASS_MISS
    }

    private fun matchesPhase(entry: AdviceEntry, phase: String): Boolean = when (phase) {
        "LEAD" -> entry.kind == AdviceKind.LEAD
        "TRAIL" -> entry.kind == AdviceKind.TRAIL
        "SPLICE" -> entry.kind == AdviceKind.SPLICE ||
            entry.kind == AdviceKind.EXCISE ||
            (entry.kind == AdviceKind.BYPASS && entry.siteSpec == null)
        else -> true
    }

    fun clear() { chains.clear(); wildcardEntries.clear() }

    /** 内部 Theatre + Resume 实现 — 链式驱动 */
    private class TheatreImpl(
        override val target: MethodCoordinate,
        override val self: Any?,
        override val args: Array<Any?>,
        val entries: List<AdviceEntry>,
        val originalInvoker: ((Array<Any?>) -> Any?)?,
    ) : Theatre, Resume {

        override val resume: Resume get() = this
        override var throwable: Throwable? = null
            private set

        internal fun setPendingThrowable(t: Throwable) { throwable = t }

        private var idx = 0
        private var skipResult: Any? = null
        private var skipped = false
        private var explicitControlUsed = false
        private var hasProceeded = false
        private var resultAvailable = false
        private var currentResult: Any? = UNSET

        override val proceeded: Boolean get() = hasProceeded

        fun runChain(): Any? {
            // 注意：不要走 proceedInternal —— 它会预置 explicitControlUsed/hasProceeded=true，
            // 导致 handleSpliceLike 无法识别"用户在 handler 内是否调用过 proceed()"，
            // 进而对所有 @Splice 抛出 ResumeMissing。runChain 只负责进入链，不算用户显式放行。
            val result = continueChain(useExistingResult = false)
            return if (skipped) skipResult else result
        }

        /**
         * 执行 advice 上挂载的字符串编译谓词。失败：
         * - 默认：warnOnce + 视为 true（不阻断）。这样运行期谓词异常不会让 advice 静默失活。
         * - `-Dincision.predicate.strict=true`：抛 [Trauma.Predicate.RuntimeFailure]。
         */
        private fun evalCompiledPredicate(cur: AdviceEntry): Boolean {
            val pred = cur.compiledPredicate ?: return true
            val ctx = EvalContextImpl(this, target, resultProvider = {
                if (resultAvailable && currentResult !== UNSET) currentResult else null
            })
            return try {
                pred.test(ctx)
            } catch (t: Throwable) {
                if (predicateStrict) {
                    throw Trauma.Predicate.RuntimeFailure(cur.predicateSource ?: "<unknown>", cur.id, t)
                }
                warnOnce(cur.id) {
                    Forensics.warn(
                        "[predicate] advice=${cur.id} test 抛异常被忽略（strict=false）: ${t.message}\n" +
                                "  source: ${cur.predicateSource ?: "<unknown>"}"
                    )
                }
                true
            }
        }

        override fun proceed(): Any? = proceedInternal(args, useExistingResult = resultAvailable)

        override fun proceed(vararg args: Any?): Any? = proceedInternal(args, useExistingResult = false)

        override fun proceedResult(returnValue: Any?): Any? {
            explicitControlUsed = true
            resultAvailable = true
            currentResult = returnValue
            return proceedInternal(args, useExistingResult = true)
        }

        override fun skip(returnValue: Any?): Any? {
            explicitControlUsed = true
            skipResult = returnValue
            skipped = true
            return returnValue
        }

        private fun proceedInternal(callArgs: Array<out Any?>, useExistingResult: Boolean): Any? {
            if (skipped) return skipResult
            applyArgs(callArgs)
            explicitControlUsed = true
            hasProceeded = true
            return continueChain(useExistingResult)
        }

        private fun continueChain(useExistingResult: Boolean): Any? {
            if (skipped) return skipResult
            if (idx >= entries.size) {
                return finalizeLeaf(useExistingResult)
            }
            val cur = entries[idx++]
            if (!evalCompiledPredicate(cur)) {
                return continueChain(useExistingResult)
            }
            if (cur.predicate != null && !cur.predicate.invoke(this)) {
                return continueChain(useExistingResult)
            }
            return try {
                when (cur.kind) {
                    AdviceKind.LEAD -> handleLead(cur)
                    AdviceKind.TRAIL -> handleTrail(cur)
                    AdviceKind.SPLICE, AdviceKind.GRAFT, AdviceKind.BYPASS,
                    AdviceKind.TRIM, AdviceKind.EXCISE -> handleSpliceLike(cur)
                }
            } catch (t: Throwable) {
                Forensics.report(Trauma.Runtime.HandlerThrew(cur.id, target, t))
                throw t
            }
        }

        private fun handleLead(cur: AdviceEntry): Any? {
            cur.handler(this)
            if (skipped) return skipResult
            return continueChain(useExistingResult = resultAvailable)
        }

        private fun handleTrail(cur: AdviceEntry): Any? {
            val downstream = continueChain(useExistingResult = resultAvailable)
            if (skipped) return skipResult
            if (!resultAvailable) {
                currentResult = downstream
                resultAvailable = true
            }
            cur.handler(this)
            return if (skipped) skipResult else currentResolvedResult()
        }

        private fun handleSpliceLike(cur: AdviceEntry): Any? {
            val explicitBefore = explicitControlUsed
            val idxBefore = idx
            val resultAvailableBefore = resultAvailable
            val currentResultBefore = currentResult
            val result = cur.handler(this)
            if (skipped) return skipResult
            if (explicitControlUsed != explicitBefore || idx != idxBefore) {
                return if (skipped) skipResult else currentResolvedResult()
            }
            if (result != null) {
                return skip(result)
            }
            if (cur.explicitResumeRequired) {
                throw Trauma.Runtime.ResumeMissing(cur.id, target)
            }
            resultAvailable = resultAvailableBefore
            currentResult = currentResultBefore
            return continueChain(useExistingResult = resultAvailable)
        }

        private fun finalizeLeaf(useExistingResult: Boolean): Any? {
            if (skipped) return skipResult
            if (useExistingResult && resultAvailable && currentResult !== UNSET) {
                return currentResolvedResult()
            }
            val invoked = originalInvoker?.invoke(this.args)
            currentResult = invoked
            resultAvailable = true
            return invoked
        }

        private fun currentResolvedResult(): Any? = if (resultAvailable && currentResult !== UNSET) currentResult else null

        internal fun wasIntercepted(): Boolean = skipped || explicitControlUsed

        private fun applyArgs(callArgs: Array<out Any?>) {
            val limit = minOf(callArgs.size, args.size)
            for (i in 0 until limit) {
                args[i] = callArgs[i]
            }
        }

        companion object {
            private val UNSET = Any()
        }
    }
}
