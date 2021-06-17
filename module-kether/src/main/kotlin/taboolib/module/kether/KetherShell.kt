package taboolib.module.kether

import io.izzel.kether.common.api.Quest
import io.izzel.kether.common.util.LocalizedException
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
        context: Consumer<ScriptContext>
    ): CompletableFuture<Any?> {
        return eval(source.joinToString("\n"), cacheScript, namespace, mainCache) {
            context.accept(this)
        }
    }

    @Throws(LocalizedException::class)
    fun eval(
        source: String,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        context: Consumer<ScriptContext>
    ): CompletableFuture<Any?> {
        return eval(source, cacheScript, namespace, mainCache) {
            context.accept(this)
        }
    }

    @Throws(LocalizedException::class)
    fun eval(
        source: List<String>,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        context: ScriptContext.() -> Unit = {}
    ): CompletableFuture<Any?> {
        return eval(source.joinToString("\n"), cacheScript, namespace, mainCache, context)
    }

    @Throws(LocalizedException::class)
    fun eval(
        source: String,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        context: ScriptContext.() -> Unit = {}
    ): CompletableFuture<Any?> {
        return eval(source, cacheScript, namespace, mainCache, context)
    }

    @Throws(LocalizedException::class)
    fun eval(
        source: String,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        cache: Cache = mainCache,
        context: ScriptContext.() -> Unit = {}
    ): CompletableFuture<Any?> {
        val s = if (source.startsWith("def")) source else "def main = { $source }"
        val script = if (cacheScript) cache.scriptMap.computeIfAbsent(s) {
            ScriptLoader.load(it, namespace)
        } else {
            ScriptLoader.load(s, namespace)
        }
        return ScriptContext.create(script).also(context).runActions()
    }

    class Cache {

        val scriptMap = ConcurrentHashMap<String, Quest>()
    }
}