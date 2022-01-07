package taboolib.module.kether

import taboolib.common.Isolated
import taboolib.common.platform.ProxyCommandSender
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Isolated
object KetherShell {

    val mainCache = Cache()

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

    class VariableMap(vararg val map: Pair<String, Any?>)

    class Cache {

        val scriptMap = ConcurrentHashMap<String, Script>()
    }
}