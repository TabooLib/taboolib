package taboolib.module.incision.weaver.site.recorder

import org.objectweb.asm.MethodVisitor

/**
 * 录制动作回放器。
 *
 * 作用：持有 [RecordingMethodVisitor.actions] 的可变副本，允许 planner/emitter 阶段在特定索引位置
 * 插入新 [InsnAction]（例如 BEFORE+offset 的延迟织入、InsnPattern 锚定后的 graft），最后把完整序列
 * 回放到目标 MethodVisitor（通常为 ClassWriter 提供的 MethodVisitor）。
 *
 * 索引语义：
 *  - 所有 insert* 使用录制时的原始索引。后续插入会导致列表整体前移，但 insert* 按输入顺序累加偏移，
 *    由 [replay] 统一处理（这里简化实现：每次插入立即 commit，插入多次时调用方需注意 idx 漂移，
 *    推荐从大到小插入，或使用 [replayWithPlan]）。
 */
class Replayer(actions: List<InsnAction>) {

    private val buffer: MutableList<InsnAction> = ArrayList(actions)

    val size: Int get() = buffer.size

    fun get(index: Int): InsnAction = buffer[index]

    /**
     * 在原索引 idx 之前插入 action（即 action 出现在 buffer[idx] 位置，原元素后移）。
     */
    fun insertBefore(idx: Int, action: InsnAction) {
        require(idx in 0..buffer.size) { "insertBefore idx=$idx out of range ${buffer.size}" }
        buffer.add(idx, action)
    }

    /**
     * 在原索引 idx 之后插入 action（即 action 紧跟在 buffer[idx] 之后）。
     */
    fun insertAfter(idx: Int, action: InsnAction) {
        require(idx in 0 until buffer.size) { "insertAfter idx=$idx out of range ${buffer.size}" }
        buffer.add(idx + 1, action)
    }

    /**
     * 在原索引 idx 之前插入一组 action，保持它们相互的顺序。
     */
    fun insertAllBefore(idx: Int, actions: List<InsnAction>) {
        require(idx in 0..buffer.size) { "insertAllBefore idx=$idx out of range ${buffer.size}" }
        buffer.addAll(idx, actions)
    }

    /**
     * 在原索引 idx 之后插入一组 action，保持它们相互的顺序。
     */
    fun insertAllAfter(idx: Int, actions: List<InsnAction>) {
        require(idx in 0 until buffer.size) { "insertAllAfter idx=$idx out of range ${buffer.size}" }
        buffer.addAll(idx + 1, actions)
    }

    /**
     * 当前完整动作序列（只读视图）。
     */
    fun snapshot(): List<InsnAction> = buffer.toList()

    /**
     * 回放到目标 MethodVisitor。调用方负责在 replay 之前 visitCode，或者确保 actions 已含所需元数据。
     */
    fun replay(mv: MethodVisitor) {
        for (a in buffer) a.replay(mv)
    }
}
