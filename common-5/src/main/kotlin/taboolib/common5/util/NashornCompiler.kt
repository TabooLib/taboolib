@file:Isolated
@file:RuntimeDependencies(
    RuntimeDependency("org.ow2.asm:asm:9.1", test = "org.objectweb.asm.ClassVisitor"),
    RuntimeDependency("org.ow2.asm:asm-util:9.1", test = "org.objectweb.asm.util.Printer"),
    RuntimeDependency("org.ow2.asm:asm-commons:9.1", test = "org.objectweb.asm.commons.Remapper"),
    RuntimeDependency("org.openjdk.nashorn:nashorn-core:15.2", test = "jdk.nashorn.api.scripting.NashornScriptEngineFactory")
)
package taboolib.common5.util

import taboolib.common.Isolated
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
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