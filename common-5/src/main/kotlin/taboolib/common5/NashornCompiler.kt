@file:Isolated
@file:RuntimeDependencies(
    RuntimeDependency("!org.openjdk.nashorn:nashorn-core:15.2", test = "!jdk.nashorn.api.scripting.NashornScriptEngineFactory")
)
package taboolib.common5

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import taboolib.common.Isolated
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import javax.script.Compilable
import javax.script.CompiledScript
import javax.script.ScriptEngine

val scriptEngineFactory by lazy {
    NashornScriptEngineFactory()
}

val scriptEngine: ScriptEngine by lazy {
    scriptEngineFactory.scriptEngine
}

fun String.compileJS(): CompiledScript? {
    return (scriptEngine as? Compilable)?.compile(this)
}