package taboolib.module.nms.remap

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * 用于处理包含 dynamic() 调用的类访问器
 * 对类中的每个方法应用 DynamicMethodVisitor 转换
 *
 * @author sky
 */
class DynamicClassVisitor(
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
        // 计算当前方法的最大局部变量起始索引
        // 静态方法从 0 开始，实例方法从 1 开始（this）
        // 加上方法参数占用的槽位
        val isStatic = (access and Opcodes.ACC_STATIC) != 0
        val argSize = Type.getArgumentsAndReturnSizes(descriptor) shr 2
        val baseLocals = if (isStatic) argSize - 1 else argSize
        // 使用一个足够大的偏移量避免与已有局部变量冲突
        // COMPUTE_MAXS 会自动处理 maxLocals
        val safeLocalVar = baseLocals + 256
        return DynamicMethodVisitor(Opcodes.ASM9, mv, remapTranslation, safeLocalVar)
    }
}
