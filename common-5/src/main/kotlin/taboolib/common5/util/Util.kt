package taboolib.common5.util

import javax.script.Compilable
import javax.script.CompiledScript
import javax.script.ScriptEngineManager

val scriptEngineManager = ScriptEngineManager()

val scriptEngineFactory by lazy {
    scriptEngineManager.engineFactories.firstOrNull { it.engineName.contains("Nashorn") }
}

val scriptEngine by lazy {
    scriptEngineFactory?.scriptEngine
}

fun String.compileJS(): CompiledScript? {
    return (scriptEngine as? Compilable)?.compile(this)
}

fun String.printed(separator: String = ""): List<String> {
    val result = ArrayList<String>()
    var i = 0
    while (i < length) {
        if (get(i) == '§') {
            i++
        } else {
            result.add("${substring(0, i + 1)}${if (i % 2 == 1) separator else ""}")
        }
        i++
    }
    if (separator.isNotEmpty() && i % 2 == 0) {
        result.add(this)
    }
    return result
}