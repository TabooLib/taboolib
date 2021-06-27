package taboolib.module.kether

import io.izzel.kether.common.api.Quest
import io.izzel.kether.common.util.LocalizedException
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.util.VariableReader
import java.util.*
import java.util.function.Consumer
import kotlin.collections.HashMap

/**
 * your health {{player health}}, your name {{player name}}
 */
object KetherFunction {

    val mainCache = Cache()

    @Throws(LocalizedException::class)
    fun parse(
        input: String,
        cacheFunction: Boolean = false,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        cache: Cache = mainCache,
        sender: ProxyCommandSender? = null,
        context: ScriptContext.() -> Unit = {}
    ): String {
        val function = if (cacheFunction) cache.functionMap.computeIfAbsent(input) {
            input.toFunction()
        } else {
            input.toFunction()
        }
        val script = if (cacheScript) cache.scriptMap.computeIfAbsent(function.source) {
            ScriptLoader.load(it, namespace)
        } else {
            ScriptLoader.load(function.source, namespace)
        }
        val vars = ScriptContext.create(script)
            .also {
                if (sender != null) {
                    it.sender = sender
                }
                context(it)
            }.run {
                runActions()
                rootFrame().variables()
            }
        return function.element.joinToString("") {
            if (it.isFunction) {
                vars.get<Any>(it.hash).orElse("{{${it.value}}}").toString()
            } else {
                it.value
            }
        }
    }

    fun String.toFunction(): Function {
        val element = VariableReader(this, '{', '}', 2).parts.map {
            Element(it.text, it.isVariable)
        }
        return Function(element, element.filter { it.isFunction }.joinToString(" ") {
            "set ${it.hash} to ${it.value}"
        })
    }

    class Element(var value: String, var isFunction: Boolean = false) {

        val hash: String
            get() = value.hashCode().toString()
    }

    class Function(val element: List<Element>, source: String) {

        val source = if (source.startsWith("def")) source else "def main = { $source }"
    }

    class Cache {

        val scriptMap = HashMap<String, Quest>()

        val functionMap = HashMap<String, Function>()
    }
}