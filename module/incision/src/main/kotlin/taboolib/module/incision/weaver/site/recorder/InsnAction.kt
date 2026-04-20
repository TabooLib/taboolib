package taboolib.module.incision.weaver.site.recorder

import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.TypePath
import taboolib.module.incision.runtime.AdviceKind
import taboolib.module.incision.weaver.site.SiteSpec
import taboolib.module.incision.weaver.site.emitter.DispatcherEmitter

/**
 * 录制下来的单条 ASM `MethodVisitor` 调用。
 *
 * 设计目标：可在 [Replayer] 中按序回放到任意 `MethodVisitor`，且支持在录制流中间插入额外动作
 * （由 BufferingPlanner 在 BEFORE+offset 等场景下使用）。
 *
 * 注意：这里只覆盖 SiteWeaver 当前涉及的 visit* 方法 + 必需的元数据（Frame / TryCatch /
 * LocalVariable / LineNumber / Label / Maxs / End），不覆盖注解类的 visit*Annotation* —— 它们由
 * Scalpel 主 pass 直接走 ClassVisitor 处理。
 */
sealed class InsnAction {

    abstract fun replay(mv: org.objectweb.asm.MethodVisitor)

    data class Insn(val opcode: Int) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitInsn(opcode)
    }

    data class IntInsn(val opcode: Int, val operand: Int) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitIntInsn(opcode, operand)
    }

    data class VarInsn(val opcode: Int, val variable: Int) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitVarInsn(opcode, variable)
    }

    data class TypeInsn(val opcode: Int, val type: String) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitTypeInsn(opcode, type)
    }

    data class FieldInsn(val opcode: Int, val owner: String, val name: String, val descriptor: String) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitFieldInsn(opcode, owner, name, descriptor)
    }

    data class MethodInsn(
        val opcode: Int,
        val owner: String,
        val name: String,
        val descriptor: String,
        val isInterface: Boolean,
    ) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) =
            mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    data class InvokeDynamicInsn(
        val name: String,
        val descriptor: String,
        val handle: Handle,
        val args: Array<out Any>,
    ) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) =
            mv.visitInvokeDynamicInsn(name, descriptor, handle, *args)
    }

    data class JumpInsn(val opcode: Int, val label: Label) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitJumpInsn(opcode, label)
    }

    data class LabelMark(val label: Label) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitLabel(label)
    }

    data class LdcInsn(val cst: Any) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitLdcInsn(cst)
    }

    data class IincInsn(val variable: Int, val increment: Int) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitIincInsn(variable, increment)
    }

    data class TableSwitchInsn(
        val min: Int,
        val max: Int,
        val dflt: Label,
        val labels: Array<Label>,
    ) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) =
            mv.visitTableSwitchInsn(min, max, dflt, *labels)
    }

    data class LookupSwitchInsn(
        val dflt: Label,
        val keys: IntArray,
        val labels: Array<Label>,
    ) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) =
            mv.visitLookupSwitchInsn(dflt, keys, labels)
    }

    data class MultiANewArrayInsn(val descriptor: String, val numDimensions: Int) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) =
            mv.visitMultiANewArrayInsn(descriptor, numDimensions)
    }

    data class TryCatchBlock(
        val start: Label,
        val end: Label,
        val handler: Label,
        val type: String?,
    ) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) =
            mv.visitTryCatchBlock(start, end, handler, type)
    }

    data class LocalVariable(
        val name: String,
        val descriptor: String,
        val signature: String?,
        val start: Label,
        val end: Label,
        val index: Int,
    ) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) =
            mv.visitLocalVariable(name, descriptor, signature, start, end, index)
    }

    data class LineNumber(val line: Int, val start: Label) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitLineNumber(line, start)
    }

    data class Frame(
        val type: Int,
        val numLocal: Int,
        val local: Array<out Any?>?,
        val numStack: Int,
        val stack: Array<out Any?>?,
    ) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) =
            mv.visitFrame(type, numLocal, local, numStack, stack)
    }

    data class InsnAnnotation(
        val typeRef: Int,
        val typePath: TypePath?,
        val descriptor: String,
        val visible: Boolean,
        val recorded: List<AnnotationCall>,
    ) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) {
            val av = mv.visitInsnAnnotation(typeRef, typePath, descriptor, visible) ?: return
            recorded.forEach { it.replay(av) }
            av.visitEnd()
        }
    }

    data class Maxs(val maxStack: Int, val maxLocals: Int) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitMaxs(maxStack, maxLocals)
    }

    object End : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) = mv.visitEnd()
    }

    /**
     * 由 recording 路径 planner 插入的"发射 advice 调用"占位。
     * replay 期直接调 [DispatcherEmitter] 往下游 mv 写字节码——不是二次 record，
     * 也就不会再次经过本类的其它分支。
     *
     * @property kind advice 类型（GRAFT / BYPASS / TRIM），决定栈平衡发射逻辑。
     * @property site 来源 site（用于 sig 构造与日志）。
     * @property isVoid BYPASS 下锚点原指令是否为 void；GRAFT/TRIM 忽略。
     */
    data class InsertEmission(
        val kind: AdviceKind,
        val site: SiteSpec,
        val isVoid: Boolean = false,
    ) : InsnAction() {
        override fun replay(mv: org.objectweb.asm.MethodVisitor) {
            when (kind) {
                AdviceKind.GRAFT -> DispatcherEmitter.emitGraft(mv, site)
                AdviceKind.BYPASS -> DispatcherEmitter.emitBypass(mv, site, isVoid)
                AdviceKind.TRIM -> DispatcherEmitter.emitTrim(mv, site)
                else -> DispatcherEmitter.emitGraft(mv, site)
            }
        }
    }
}

/**
 * AnnotationVisitor 录制项 —— 用于 visit*Annotation* 系列方法。
 * 当前只覆盖最常见调用，必要时扩展。
 */
sealed class AnnotationCall {

    abstract fun replay(av: org.objectweb.asm.AnnotationVisitor)

    data class Value(val name: String?, val value: Any) : AnnotationCall() {
        override fun replay(av: org.objectweb.asm.AnnotationVisitor) = av.visit(name, value)
    }

    data class Enum(val name: String?, val descriptor: String, val value: String) : AnnotationCall() {
        override fun replay(av: org.objectweb.asm.AnnotationVisitor) = av.visitEnum(name, descriptor, value)
    }
}
