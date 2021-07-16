@file:Isolated
@file:RuntimeDependencies(
    RuntimeDependency("org.ow2.asm:asm:9.1", test = "org.objectweb.asm.ClassVisitor"),
    RuntimeDependency("org.ow2.asm:asm-util:9.1", test = "org.objectweb.asm.util.Printer"),
    RuntimeDependency("org.ow2.asm:asm-commons:9.1", test = "org.objectweb.asm.commons.Remapper"),
    RuntimeDependency("org.openjdk.nashorn:nashorn-core:15.2", test = "jdk.nashorn.api.scripting.NashornScriptEngineFactory")
)
package taboolib.common5.util

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