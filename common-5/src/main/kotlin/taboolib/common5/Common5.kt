package taboolib.common5

import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency

/**
 * TabooLib
 * taboolib.common5
 *
 * @author sky
 * @since 2021/6/15 4:27 下午
 */
@RuntimeDependencies(
    RuntimeDependency("org.ow2.asm:asm:9.1", test = "org.objectweb.asm.ClassVisitor"),
    RuntimeDependency("org.ow2.asm:asm-util:9.1", test = "org.objectweb.asm.util.Printer"),
    RuntimeDependency("org.ow2.asm:asm-commons:9.1", test = "org.objectweb.asm.commons.Remapper"),
    RuntimeDependency("com.google.guava:guava:21.0", test = "com.google.common.base.Optional"),
    RuntimeDependency("org.apache.commons:commons-lang3:3.5", test = "com.google.common.base.Optional"),
    RuntimeDependency("org.openjdk.nashorn:nashorn-core:15.2", test = "jdk.nashorn.api.scripting.NashornScriptEngineFactory")
)
object Common5