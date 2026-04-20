package taboolib.module.incision.gate

import taboolib.module.incision.diagnostic.Forensics
import taboolib.module.incision.diagnostic.Trauma
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 默认 IncisionGate 实现。
 *
 * 加载路径取决于 [GateBootstrapper]:
 * - 理想路径：被推进系统 ClassLoader（通过 Instrumentation.appendToSystemClassLoaderSearch）
 * - Fallback：加载在某个 incision ClassLoader 内，通过 Exchanges 共享实例；其他插件通过
 *   reflective proxy 调用该实例
 *
 * 本类只用 [IncisionGateApi] 的基础类型参数，避免跨 ClassLoader 类型泄漏。
 */
class DefaultIncisionGate(private val apiVersion: Int) : IncisionGateApi {

    private val chains = ConcurrentHashMap<String, GlobalChain>()
    private val implanted = ConcurrentHashMap<String, Boolean>()
    private val seq = AtomicLong()

    override fun apiVersion(): Int = apiVersion

    override fun supportedAdviceTypes(): Set<String> =
        setOf("LEAD", "TRAIL", "SPLICE", "GRAFT", "BYPASS", "TRIM", "EXCISE")

    override fun ensureImplanted(targetSignature: String, weaver: BytecodeWeaverProxy): Boolean {
        val prev = implanted.putIfAbsent(targetSignature, true)
        if (prev != null) return false
        // 触发 weaver.transform（调用方负责实际 retransform）
        try {
            weaver.transform(weaver.ownerClass(), ByteArray(0))
        } catch (t: Throwable) {
            Forensics.warn("IncisionGate ensureImplanted 触发 weaver 失败: ${t.message}")
        }
        return true
    }

    override fun register(targetSignature: String, advice: AdviceProxy): GlobalSutureToken {
        val chain = chains.computeIfAbsent(targetSignature) { GlobalChain(it) }
        chain.add(advice)
        val token = TokenImpl(targetSignature, advice.incisionId, advice.pluginName, seq.incrementAndGet())
        // 冲突分析
        ConflictAnalyzer.analyze(chain)
        return token
    }

    override fun unregister(token: GlobalSutureToken): Boolean {
        val chain = chains[token.targetSignature] ?: return false
        return chain.removeById(token.incisionId)
    }

    override fun dispatch(targetSignature: String, self: Any?, args: Array<Any?>): Any? {
        val chain = chains[targetSignature] ?: return null
        val ordered = chain.snapshot()
        if (ordered.isEmpty()) return null
        // 逐条调用 advice，遇到返回 non-null 的 Bypass/Excise 即视为终止
        var finalResult: Any? = null
        for (advice in ordered) {
            val r = try { advice.invoke(targetSignature, self, args) } catch (t: Throwable) {
                Forensics.error("[gate.dispatch] ${advice.incisionId} 抛出异常", t); null
            }
            if (advice.kind == "BYPASS" || advice.kind == "EXCISE" || advice.kind == "SPLICE") {
                finalResult = r
            }
        }
        return finalResult
    }

    override fun listByPlugin(pluginName: String): List<String> {
        val out = mutableListOf<String>()
        for ((_, chain) in chains) {
            out += chain.snapshot().filter { it.pluginName == pluginName }.map { it.incisionId }
        }
        return out
    }

    override fun healByClassLoader(cl: ClassLoader): Int {
        var n = 0
        for ((_, chain) in chains) n += chain.removeByClassLoader(cl)
        return n
    }

    private class GlobalChain(val targetSignature: String) {
        private val entries = java.util.concurrent.CopyOnWriteArrayList<AdviceProxy>()
        fun add(a: AdviceProxy) {
            entries.add(a)
            val sorted = entries.toMutableList().apply { sortByDescending { it.priority } }
            entries.clear()
            entries.addAll(sorted)
        }
        fun removeById(id: String): Boolean = entries.removeIf { it.incisionId == id }
        fun removeByClassLoader(cl: ClassLoader): Int {
            var n = 0
            entries.removeIf { if (it.classLoader() === cl) { n++; true } else false }
            return n
        }
        fun snapshot(): List<AdviceProxy> = entries.toList()
    }

    private data class TokenImpl(
        override val targetSignature: String,
        override val incisionId: String,
        override val pluginName: String,
        val seq: Long,
    ) : GlobalSutureToken
}

/**
 * 启动期冲突分析。
 */
object ConflictAnalyzer {

    fun analyze(chain: Any) {
        // 通过反射访问 chain.snapshot()，避免耦合 DefaultIncisionGate 的内部类类型。
        val snapshotMethod = try {
            chain.javaClass.getDeclaredMethod("snapshot").apply { isAccessible = true }
        } catch (_: Throwable) { return }
        @Suppress("UNCHECKED_CAST")
        val list = snapshotMethod.invoke(chain) as? List<AdviceProxy> ?: return

        val excises = list.filter { it.kind == "EXCISE" }
        if (excises.size > 1) {
            val target = (try { chain.javaClass.getField("targetSignature").get(chain) } catch (_: Throwable) { "?" }).toString()
            Forensics.report(Trauma.Conflict.MultipleExcise(
                taboolib.module.incision.api.MethodCoordinate(target, "", ""),
                excises.map { "${it.pluginName}:${it.incisionId}" }
            ))
        }
        val bypasses = list.filter { it.kind == "BYPASS" }
        if (bypasses.size > 1) {
            val target = (try { chain.javaClass.getField("targetSignature").get(chain) } catch (_: Throwable) { "?" }).toString()
            Forensics.report(Trauma.Conflict.BypassOverlap(
                taboolib.module.incision.api.MethodCoordinate(target, "", ""),
                bypasses.map { "${it.pluginName}:${it.incisionId}" }
            ))
        }
    }
}
