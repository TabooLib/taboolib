package taboolib.module.nms.remap

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 用于处理包含 require 调用的类访问器
 * 对类中的每个方法应用 RequireMethodVisitor 转换
 *
 * @author mical
 * @since 2024/10/26
 */
class RequireClassVisitor(
    api: Int,
    classVisitor: ClassVisitor,
    private val remapTranslation: RemapTranslation
) : ClassVisitor(api, classVisitor) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        // 对每个方法应用 require 转换
        return RequireMethodVisitor(Opcodes.ASM9, mv, remapTranslation)
    }
}

