package taboolib.module.nms.remap

import org.objectweb.asm.Handle
import org.objectweb.asm.Label

// 缓冲的指令数据结构
sealed class BufferedInstruction {

    data class Ldc(val value: Any?) : BufferedInstruction()

    data class MethodInsn(
        val opcode: Int,
        val owner: String?,
        val name: String?,
        val descriptor: String?,
        val isInterface: Boolean
    ) : BufferedInstruction()

    data class FieldInsn(
        val opcode: Int,
        val owner: String?,
        val name: String?,
        val descriptor: String?
    ) : BufferedInstruction()

    data class TypeInsn(val opcode: Int, val type: String?) : BufferedInstruction()

    data class Insn(val opcode: Int) : BufferedInstruction()

    data class VarInsn(val opcode: Int, val varIndex: Int) : BufferedInstruction()

    data class JumpInsn(val opcode: Int, val label: Label?) : BufferedInstruction()

    data class LabelInsn(val label: Label?) : BufferedInstruction()

    data class InvokeDynamicInsn(
        val name: String?,
        val descriptor: String?,
        val bootstrapMethodHandle: Handle?,
        val bootstrapMethodArguments: Array<out Any?>
    ) : BufferedInstruction() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is InvokeDynamicInsn) return false
            if (name != other.name) return false
            if (descriptor != other.descriptor) return false
            if (bootstrapMethodHandle != other.bootstrapMethodHandle) return false
            if (!bootstrapMethodArguments.contentEquals(other.bootstrapMethodArguments)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = name?.hashCode() ?: 0
            result = 31 * result + (descriptor?.hashCode() ?: 0)
            result = 31 * result + (bootstrapMethodHandle?.hashCode() ?: 0)
            result = 31 * result + bootstrapMethodArguments.contentHashCode()
            return result
        }
    }
}