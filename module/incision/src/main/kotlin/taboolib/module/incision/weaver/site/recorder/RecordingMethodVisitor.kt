package taboolib.module.incision.weaver.site.recorder

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.TypePath

/**
 * 录制型 MethodVisitor：把所有 visit* 调用转换为 [InsnAction] 追加到 [actions]，
 * 同时把调用向 `next` 转发，使录制与原有 streaming 管线并行存在。
 *
 * 典型用法：在需要 buffering 的 advice（offset != 0 或 InsnPattern）场景下，
 * 把 Scalpel 下游的 MethodVisitor 包一层 RecordingMethodVisitor，结束后由 [Replayer]
 * 对 actions 做 insertBefore/insertAfter 再回放到最终的 ClassWriter。
 *
 * 注意：为避免在 Scalpel 主 pass 中丢失 Label 关联，录制器采用 "record-then-forward" 策略，
 * next 可为 null —— 此时仅录制，不转发。
 */
open class RecordingMethodVisitor(
    api: Int = Opcodes.ASM9,
    private val next: MethodVisitor? = null,
) : MethodVisitor(api, next) {

    val actions: MutableList<InsnAction> = ArrayList()

    private fun record(action: InsnAction) {
        actions += action
    }

    override fun visitInsn(opcode: Int) {
        record(InsnAction.Insn(opcode))
        super.visitInsn(opcode)
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        record(InsnAction.IntInsn(opcode, operand))
        super.visitIntInsn(opcode, operand)
    }

    override fun visitVarInsn(opcode: Int, variable: Int) {
        record(InsnAction.VarInsn(opcode, variable))
        super.visitVarInsn(opcode, variable)
    }

    override fun visitTypeInsn(opcode: Int, type: String) {
        record(InsnAction.TypeInsn(opcode, type))
        super.visitTypeInsn(opcode, type)
    }

    override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        record(InsnAction.FieldInsn(opcode, owner, name, descriptor))
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitMethodInsn(
        opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean
    ) {
        record(InsnAction.MethodInsn(opcode, owner, name, descriptor, isInterface))
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    override fun visitInvokeDynamicInsn(
        name: String, descriptor: String, bootstrap: Handle, vararg args: Any
    ) {
        @Suppress("UNCHECKED_CAST")
        record(InsnAction.InvokeDynamicInsn(name, descriptor, bootstrap, args))
        super.visitInvokeDynamicInsn(name, descriptor, bootstrap, *args)
    }

    override fun visitJumpInsn(opcode: Int, label: Label) {
        record(InsnAction.JumpInsn(opcode, label))
        super.visitJumpInsn(opcode, label)
    }

    override fun visitLabel(label: Label) {
        record(InsnAction.LabelMark(label))
        super.visitLabel(label)
    }

    override fun visitLdcInsn(value: Any) {
        record(InsnAction.LdcInsn(value))
        super.visitLdcInsn(value)
    }

    override fun visitIincInsn(variable: Int, increment: Int) {
        record(InsnAction.IincInsn(variable, increment))
        super.visitIincInsn(variable, increment)
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label, vararg labels: Label) {
        val copied: Array<Label> = Array(labels.size) { labels[it] }
        record(InsnAction.TableSwitchInsn(min, max, dflt, copied))
        super.visitTableSwitchInsn(min, max, dflt, *labels)
    }

    override fun visitLookupSwitchInsn(dflt: Label, keys: IntArray, labels: Array<out Label>) {
        @Suppress("UNCHECKED_CAST")
        record(InsnAction.LookupSwitchInsn(dflt, keys.copyOf(), (labels as Array<Label>).copyOf()))
        super.visitLookupSwitchInsn(dflt, keys, labels)
    }

    override fun visitMultiANewArrayInsn(descriptor: String, numDimensions: Int) {
        record(InsnAction.MultiANewArrayInsn(descriptor, numDimensions))
        super.visitMultiANewArrayInsn(descriptor, numDimensions)
    }

    override fun visitTryCatchBlock(start: Label, end: Label, handler: Label, type: String?) {
        record(InsnAction.TryCatchBlock(start, end, handler, type))
        super.visitTryCatchBlock(start, end, handler, type)
    }

    override fun visitLocalVariable(
        name: String, descriptor: String, signature: String?, start: Label, end: Label, index: Int
    ) {
        record(InsnAction.LocalVariable(name, descriptor, signature, start, end, index))
        super.visitLocalVariable(name, descriptor, signature, start, end, index)
    }

    override fun visitLineNumber(line: Int, start: Label) {
        record(InsnAction.LineNumber(line, start))
        super.visitLineNumber(line, start)
    }

    override fun visitFrame(
        type: Int, numLocal: Int, local: Array<out Any?>?, numStack: Int, stack: Array<out Any?>?
    ) {
        record(InsnAction.Frame(type, numLocal, local?.copyOf(), numStack, stack?.copyOf()))
        super.visitFrame(type, numLocal, local, numStack, stack)
    }

    override fun visitInsnAnnotation(
        typeRef: Int, typePath: TypePath?, descriptor: String, visible: Boolean
    ): AnnotationVisitor? {
        val calls = ArrayList<AnnotationCall>()
        val delegate = next?.visitInsnAnnotation(typeRef, typePath, descriptor, visible)
        record(InsnAction.InsnAnnotation(typeRef, typePath, descriptor, visible, calls))
        return RecordingAnnotationVisitor(api, delegate, calls)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        record(InsnAction.Maxs(maxStack, maxLocals))
        super.visitMaxs(maxStack, maxLocals)
    }

    override fun visitEnd() {
        record(InsnAction.End)
        super.visitEnd()
    }
}

/**
 * 内部用的 AnnotationVisitor 录制壳：把 visit/visitEnum 调用转化成 [AnnotationCall]。
 * 其它 visit*（Array/Annotation 嵌套）目前未覆盖，需要时补充。
 */
private class RecordingAnnotationVisitor(
    api: Int,
    next: AnnotationVisitor?,
    private val calls: MutableList<AnnotationCall>,
) : AnnotationVisitor(api, next) {

    override fun visit(name: String?, value: Any) {
        calls += AnnotationCall.Value(name, value)
        super.visit(name, value)
    }

    override fun visitEnum(name: String?, descriptor: String, value: String) {
        calls += AnnotationCall.Enum(name, descriptor, value)
        super.visitEnum(name, descriptor, value)
    }
}
