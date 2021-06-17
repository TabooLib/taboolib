package taboolib.module.kether

import io.izzel.kether.common.api.Quest
import io.izzel.kether.common.util.LocalizedException
import java.util.*
import java.util.function.Consumer
import kotlin.collections.HashMap

/**
 * your health {{player health}}, your name {{player name}}
 */
object KetherFunction {

    val regex = Regex("\\{\\{(.*?)}}")

    val mainCache = Cache()

    @Throws(LocalizedException::class)
    fun parse(
        input: String,
        cacheFunction: Boolean = false,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        context: Consumer<ScriptContext>
    ): String {
        return parse(input, cacheFunction, cacheScript, namespace, mainCache) {
            context.accept(this)
        }
    }

    @Throws(LocalizedException::class)
    fun parse(
        input: String,
        cacheFunction: Boolean = false,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        context: ScriptContext.() -> Unit = {}
    ): String {
        return parse(input, cacheFunction, cacheScript, namespace, mainCache, context)
    }

    @Throws(LocalizedException::class)
    fun parse(
        input: String,
        cacheFunction: Boolean = false,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        cache: Cache = mainCache,
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
        val vars = ScriptContext.create(script).also(context).run {
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
        val element = ArrayList<Element>()
        var index = 0
        regex.findAll(this).forEach {
            element.add(Element(substring(index, it.range.first)))
            element.add(Element(it.groupValues[1], true))
            index = it.range.last + 1
        }
        val last = Element(substring(index, length))
        if (last.value.isNotEmpty()) {
            element.add(last)
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