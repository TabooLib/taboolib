package taboolib.module.kether

import io.izzel.kether.common.api.Quest
import io.izzel.kether.common.util.LocalizedException
import taboolib.common.platform.ProxyCommandSender
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

object KetherShell {

    val mainCache = Cache()

    @Throws(LocalizedException::class)
    fun eval(
        source: List<String>,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        cache: Cache = mainCache,
        sender: ProxyCommandSender? = null,
        context: ScriptContext.() -> Unit = {}
    ): CompletableFuture<Any?> {
        return eval(source.joinToString("\n"), cacheScript, namespace, cache, sender, context)
    }

    @Throws(LocalizedException::class)
    fun eval(
        source: String,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        cache: Cache = mainCache,
        sender: ProxyCommandSender? = null,
        context: ScriptContext.() -> Unit = {}
    ): CompletableFuture<Any?> {
        val s = if (source.startsWith("def")) source else "def main = { $source }"
        val script = if (cacheScript) cache.scriptMap.computeIfAbsent(s) {
            ScriptLoader.load(it, namespace)
        } else {
            ScriptLoader.load(s, namespace)
        }
        return ScriptContext.create(script).also {
            if (sender != null) {
                it.sender = sender
            }
            context(it)
        }.runActions()
    }

    class Cache {

        val scriptMap = ConcurrentHashMap<String, Quest>()
    }
}