package taboolib.module.nms.remap

import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import taboolib.common.reflect.ClassHelper
import java.util.*

/**
 * 用于处理 require 函数调用的方法访问器
 * 检测并替换 require(SomeClass::class.java) 模式
 *
 * 工作流程：
 * 1. 检测到类常量加载 (LDC)
 * 2. 缓冲后续指令
 * 3. 检测到 ::class.java 字段访问 (GETSTATIC)
 * 4. 检测到 require 方法调用 (INVOKESTATIC)
 * 5. 提取类名，进行转译，检查类是否存在
 * 6. 替换整个调用链为布尔常量 (ICONST_1 或 ICONST_0)
 */
class RequireMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor?,
    private val remapTranslation: RemapTranslation,
) : MethodVisitor(api, methodVisitor) {

    // 指令缓冲区，用于识别 require 调用模式
    private val instructionBuffer = LinkedList<BufferedInstruction>()
    private var bufferingMode = false

    override fun visitLdcInsn(value: Any?) {
        // 检测类常量加载 (LDC 指令加载 Class 对象)
        if (value is Type && value.sort == Type.OBJECT) {
            // 开始缓冲模式，准备检测后续的 require 调用
            startBuffering()
            instructionBuffer.add(BufferedInstruction.Ldc(value))
            return
        }
        if (bufferingMode) {
            // 在缓冲模式中遇到非预期的 LDC 指令，刷新缓冲区
            flushBuffer()
        }
        super.visitLdcInsn(value)
    }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        if (bufferingMode) {
            // 检测 ::class.java 的 GETSTATIC 指令
            if (opcode == Opcodes.GETSTATIC && name == "class" && descriptor == "Ljava/lang/Class;") {
                instructionBuffer.add(BufferedInstruction.FieldInsn(opcode, owner, name, descriptor))
                return
            } else {
                // 不是预期的指令，刷新缓冲区
                flushBuffer()
            }
        }
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean,
    ) {
        if (bufferingMode) {
            // 检查是否是 require 调用
            if (owner == "taboolib/module/nms/remap/RemapRequireKt" && name == "require" && descriptor == "(Ljava/lang/Class;)Z") {
                // 处理 require 调用并替换为布尔常量
                processRequireCall()
                bufferingMode = false
                return
            } else {
                // 不是 require 调用，刷新缓冲区
                flushBuffer()
            }
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    override fun visitInsn(opcode: Int) {
        if (bufferingMode) {
            flushBuffer()
        }
        super.visitInsn(opcode)
    }

    override fun visitVarInsn(opcode: Int, varIndex: Int) {
        if (bufferingMode) {
            flushBuffer()
        }
        super.visitVarInsn(opcode, varIndex)
    }

    override fun visitTypeInsn(opcode: Int, type: String?) {
        if (bufferingMode) {
            flushBuffer()
        }
        super.visitTypeInsn(opcode, type)
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) {
        if (bufferingMode) {
            flushBuffer()
        }
        super.visitJumpInsn(opcode, label)
    }

    override fun visitLabel(label: Label?) {
        if (bufferingMode) {
            flushBuffer()
        }
        super.visitLabel(label)
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        if (bufferingMode) {
            flushBuffer()
        }
        super.visitIntInsn(opcode, operand)
    }

    override fun visitIincInsn(varIndex: Int, increment: Int) {
        if (bufferingMode) {
            flushBuffer()
        }
        super.visitIincInsn(varIndex, increment)
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
        if (bufferingMode) {
            flushBuffer()
        }
        super.visitTableSwitchInsn(min, max, dflt, *labels)
    }

    override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
        if (bufferingMode) {
            flushBuffer()
        }
        super.visitLookupSwitchInsn(dflt, keys, labels)
    }

    override fun visitMultiANewArrayInsn(descriptor: String?, numDimensions: Int) {
        if (bufferingMode) {
            flushBuffer()
        }
        super.visitMultiANewArrayInsn(descriptor, numDimensions)
    }

    override fun visitInvokeDynamicInsn(
        name: String?,
        descriptor: String?,
        bootstrapMethodHandle: Handle?,
        vararg bootstrapMethodArguments: Any?,
    ) {
        if (bufferingMode) {
            flushBuffer()
        }
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments)
    }

    /**
     * 开始缓冲模式
     */
    private fun startBuffering() {
        bufferingMode = true
        instructionBuffer.clear()
    }

    /**
     * 处理检测到的 require 调用
     * 将整个 require 调用替换为布尔常量
     */
    private fun processRequireCall() {
        // 从缓冲区提取类名
        val className = extractClassName()
        if (className != null) {
            // 转译类名 (使用 RemapTranslation 的转译逻辑)
            val translatedClassName = remapTranslation.translate(className.replace('.', '/')).replace('/', '.')
            // 检查类是否存在
            val classExists = checkClassExists(translatedClassName)
            // 清空缓冲区（不输出原始指令）
            instructionBuffer.clear()
            // 直接输出布尔常量
            // ICONST_1 表示 true，ICONST_0 表示 false
            super.visitInsn(if (classExists) Opcodes.ICONST_1 else Opcodes.ICONST_0)
        } else {
            // 如果无法提取类名，刷新原始指令（保持原样）
            flushBuffer()
        }
    }

    /**
     * 从缓冲区提取类名
     * 识别模式：LDC <Type> -> GETSTATIC ::class.java
     */
    private fun extractClassName(): String? {
        if (instructionBuffer.isEmpty()) return null
        // 检查第一条指令是否为 LDC Type
        val firstInstruction = instructionBuffer.firstOrNull()
        if (firstInstruction is BufferedInstruction.Ldc) {
            val value = firstInstruction.value
            if (value is Type && value.sort == Type.OBJECT) {
                return value.className
            }
        }
        return null
    }

    /**
     * 检查类是否存在
     * 使用 ClassHelper 尝试加载类
     */
    private fun checkClassExists(className: String): Boolean {
        return try {
            // 尝试加载类
            ClassHelper.getClass(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        } catch (e: NoClassDefFoundError) {
            false
        } catch (e: LinkageError) {
            false
        } catch (e: Throwable) {
            // 其他异常也视为类不存在
            false
        }
    }

    /**
     * 刷新缓冲区，输出所有缓冲的指令
     * 当检测到不符合 require 模式的指令时调用
     */
    private fun flushBuffer() {
        for (instruction in instructionBuffer) {
            when (instruction) {
                is BufferedInstruction.Ldc -> super.visitLdcInsn(instruction.value)
                is BufferedInstruction.MethodInsn -> super.visitMethodInsn(
                    instruction.opcode,
                    instruction.owner,
                    instruction.name,
                    instruction.descriptor,
                    instruction.isInterface
                )

                is BufferedInstruction.FieldInsn -> super.visitFieldInsn(instruction.opcode, instruction.owner, instruction.name, instruction.descriptor)
                is BufferedInstruction.TypeInsn -> super.visitTypeInsn(instruction.opcode, instruction.type)
                is BufferedInstruction.Insn -> super.visitInsn(instruction.opcode)
                is BufferedInstruction.VarInsn -> super.visitVarInsn(instruction.opcode, instruction.varIndex)
                is BufferedInstruction.JumpInsn -> super.visitJumpInsn(instruction.opcode, instruction.label)
                is BufferedInstruction.LabelInsn -> super.visitLabel(instruction.label)
                is BufferedInstruction.InvokeDynamicInsn -> super.visitInvokeDynamicInsn(
                    instruction.name,
                    instruction.descriptor,
                    instruction.bootstrapMethodHandle,
                    *instruction.bootstrapMethodArguments
                )
            }
        }
        instructionBuffer.clear()
        bufferingMode = false
    }
}
