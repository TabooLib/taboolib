package taboolib.module.kether

import taboolib.common.Isolated
import taboolib.common.platform.ProxyCommandSender
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Isolated
object KetherShell {

    val mainCache = Cache()

    fun eval(source: List<String>, options: ScriptOptions = ScriptOptions()): CompletableFuture<Any?> {
        return eval(source.joinToString("\n"), options)
    }

    fun eval(source: String, options: ScriptOptions = ScriptOptions()): CompletableFuture<Any?> {
        fun process() = eval(source, options.useCache, options.namespace, options.cache, options.sender, options.vars, options.context)
        return if (options.exception) process() else runKether { process() } ?: CompletableFuture.completedFuture(null)
    }

    fun eval(
        source: List<String>,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        cache: Cache = mainCache,
        sender: ProxyCommandSender? = null,
        vars: VariableMap? = null,
        context: ScriptContext.() -> Unit = {},
    ): CompletableFuture<Any?> {
        return eval(source.joinToString("\n"), cacheScript, namespace, cache, sender, vars, context)
    }

    fun eval(
        source: String,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        cache: Cache = mainCache,
        sender: ProxyCommandSender? = null,
        vars: VariableMap? = null,
        context: ScriptContext.() -> Unit = {},
    ): CompletableFuture<Any?> {
        val s = if (source.startsWith("def ")) source else "def main = { $source }"
        val script = if (cacheScript) cache.scriptMap.computeIfAbsent(s) {
            it.parseKetherScript(namespace)
        } else {
            s.parseKetherScript(namespace)
        }
        return ScriptContext.create(script).also {
            if (sender != null) {
                it.sender = sender
            }
            vars?.map?.forEach { (k, v) -> it.rootFrame().variables()[k] = v }
            context(it)
        }.runActions()
    }

    class VariableMap(val map: Map<String, Any?>) {

        constructor(vararg map: Pair<String, Any?>) : this(map.toMap())
    }

    class Cache {

        val scriptMap = ConcurrentHashMap<String, Script>()
    }
}