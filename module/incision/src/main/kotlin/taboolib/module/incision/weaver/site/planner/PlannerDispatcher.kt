package taboolib.module.incision.weaver.site.planner

import taboolib.module.incision.api.Shift
import taboolib.module.incision.weaver.site.SiteSpec
import taboolib.module.incision.weaver.site.matcher.MatchEvent

/**
 * 发射策略规划器 —— 根据 site 的 anchor / shift / offset 把 [MatchEvent] 归为三种 [EmissionPlan.Strategy]。
 *
 * 策略矩阵（offset 的绝对值用于 skipForward/skipBackward 计数）：
 *  - `anchorIndex >= 0`（OpcodeSeq 命中走 recording 路径）→ BUFFERING，保持语义：插入在序列末指令之前
 *  - `offset > 0 && shift == AFTER` → DEFERRED，锚点 + offset 条真实指令后
 *  - `offset > 0 && shift == BEFORE` → BUFFERING，锚点 - offset 条真实指令前（反向跳跃）
 *  - `offset < 0` → BUFFERING，锚点 - |offset| 条真实指令前（shift 方向不再影响）
 *  - HEAD 锚点（由 SiteWeaver 单独走 headEvents 流程插入）→ 这里不命中，但保留 IMMEDIATE 兜底
 *  - 其余 `offset == 0` → IMMEDIATE，由 [SiteWeaver.applyPlan.insertAtAnchor] 按 shift/kind 就地插入
 */
class PlannerDispatcher {

    fun plan(event: MatchEvent): EmissionPlan {
        val spec = event.siteSpec
        val offset = spec.offset
        return when {
            // 先按 offset 路由：非零 offset 的语义覆盖任何 anchorIndex/pattern 出处
            offset > 0 && spec.shift == Shift.AFTER -> EmissionPlan(event, EmissionPlan.Strategy.DEFERRED)
            offset != 0 -> EmissionPlan(event, EmissionPlan.Strategy.BUFFERING)
            // OpcodeSeq 命中（pattern 为 OpcodeSeq 且 anchorIndex 指向序列末）→ BUFFERING：插入在序列末前
            event.anchorIndex >= 0 && spec.pattern is taboolib.module.incision.weaver.site.pattern.SitePattern.OpcodeSeq ->
                EmissionPlan(event, EmissionPlan.Strategy.BUFFERING)
            // 其余 offset == 0 场景 → IMMEDIATE，由 SiteWeaver 按 shift/kind 就地插入
            else -> EmissionPlan(event, EmissionPlan.Strategy.IMMEDIATE)
        }
    }

    /** 批量规划便捷入口，保留顺序。 */
    fun planAll(events: List<MatchEvent>): List<EmissionPlan> = events.map(::plan)

    companion object {

        /**
         * 判断整组 sites 是否允许全走 streaming 路径（无需 recording 开销）。
         * 条件：没有 OpcodeSeq 类型的 site（pattern 判定由调用方事先完成）。
         */
        fun canStream(sites: List<SiteSpec>, hasOpcodeSeq: Boolean): Boolean {
            if (sites.isEmpty()) return true
            if (hasOpcodeSeq) return false
            // BYPASS 需要稳定且不与宿主局部变量冲突的 tmp slot 来保存 dispatch 返回的 Object。
            // streaming 路径在 visitMethodInsn 时就要 ASTORE，但此时还没看到宿主方法所有 VarInsn，
            // 拿不到真实 maxLocals——只能按 args 估算，会撞上方法体内声明的局部（曾观察到
            // invokeHelper(I)I 的 tmp 落在 slot 2 == 局部 a，导致 IRETURN 时 Expected I, but found R）。
            // 走 recording 路径时 TreeMethodDriver 已在 visitEnd 拿到完整 MethodNode.maxLocals，
            // resolveBypassMeta 会安全地把 tmpSlot 分配在所有局部之上。
            if (sites.any { it.kind == taboolib.module.incision.runtime.AdviceKind.BYPASS }) return false
            return true
        }
    }
}
