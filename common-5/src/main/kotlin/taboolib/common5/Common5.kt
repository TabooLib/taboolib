package taboolib.common5

import taboolib.common.platform.PlatformInstance
import taboolib.module.dependency.RuntimeDependencies
import taboolib.module.dependency.RuntimeDependency
import taboolib.module.dependency.RuntimeName
import taboolib.module.dependency.RuntimeTest

/**
 * TabooLib
 * taboolib.common5
 *
 * @author sky
 * @since 2021/6/15 4:27 下午
 */
@RuntimeDependencies(
    RuntimeDependency(group = "org.ow2.asm", id = "asm", version = "9.1", hash = "a99500cf6eea30535eeac6be73899d048f8d12a8"),
    RuntimeDependency(group = "org.ow2.asm", id = "asm-commons", version = "9.1", hash = "8b971b182eb5cf100b9e8d4119152d83e00e0fdd")
)
@RuntimeName(group = "org.ow2.asm", name = "asm (9.1)")
@RuntimeTest(group = "org.ow2.asm", path = "org.objectweb.asm.ClassVisitor")
@PlatformInstance
object Common5 {

}