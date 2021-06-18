package taboolib.common5

import taboolib.common.platform.Awake
import taboolib.module.dependency.*

/**
 * TabooLib
 * taboolib.common5
 *
 * @author sky
 * @since 2021/6/15 4:27 下午
 */
@RuntimeDependencies(
    RuntimeDependency(group = "org.ow2.asm", id = "asm", version = "9.1", hash = "a99500cf6eea30535eeac6be73899d048f8d12a8"),
    RuntimeDependency(group = "org.ow2.asm", id = "asm-util", version = "9.1", hash = "36464a45d871779f3383a8a9aba2b26562a86729"),
    RuntimeDependency(group = "org.ow2.asm", id = "asm-commons", version = "9.1", hash = "8b971b182eb5cf100b9e8d4119152d83e00e0fdd"),
    RuntimeDependency(group = "com.google.guava", id = "guava", version = "21.0", hash = "3a3d111be1be1b745edfa7d91678a12d7ed38709"),
    RuntimeDependency(group = "org.apache.commons", id = "commons-lang3", version = "3.5", hash = "6c6c702c89bfff3cd9e80b04d668c5e190d588c6"),
    RuntimeDependency(group = "org.openjdk.nashorn", id = "nashorn-core", version = "15.2", hash = "1b67a6139e8e4a51ceaa3a0c836c48a3f1e26fca")
)
@RuntimeNames(
    RuntimeName(group = "org.ow2.asm", name = "asm (9.1)"),
    RuntimeName(group = "com.google.guava", name = "Guava (21.0)"),
    RuntimeName(group = "org.apache.commons", name = "Apache Commons (3.5)"),
    RuntimeName(group = "org.openjdk.nashorn", name = "Oracle Nashorn (15.2)")
)
@RuntimeTests(
    RuntimeTest(group = "org.ow2.asm", path = ["org.objectweb.asm.ClassVisitor", "org.objectweb.asm.util.Printer"]),
    RuntimeTest(group = "com.google.guava", path = ["com.google.common.base.Optional"]),
    RuntimeTest(group = "org.apache.commons", path = ["org.apache.commons.lang3.Validate"]),
    RuntimeTest(group = "org.openjdk.nashorn", path = ["jdk.nashorn.api.scripting.NashornScriptEngineFactory"])
)
@Awake
object Common5 {

}