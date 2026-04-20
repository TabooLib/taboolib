package taboolib.module.incision.weaver.site.planner

import taboolib.module.incision.weaver.site.SiteSpec
import taboolib.module.incision.weaver.site.matcher.MatchEvent

/**
 * 发射计划 —— 由 planner 根据 [MatchEvent] 结合 site 特性（offset / pattern 种类）产出，
 * emitter 据此决定何时把 dispatcher 调用写入字节码流。
 *
 * 三种策略：
 *  - [Strategy.IMMEDIATE]：锚点原位（offset == 0，非 OpcodeSeq），可以在 streaming 路径直接发射；
 *  - [Strategy.DEFERRED]：AFTER + offset > 0，需要在流过若干条后续指令后触发；
 *  - [Strategy.BUFFERING]：BEFORE + offset > 0（或任何需要向前看的场景），必须走 recording 路径。
 */
data class EmissionPlan(
    val event: MatchEvent,
    val strategy: Strategy,
) {

    val site: SiteSpec get() = event.siteSpec

    enum class Strategy { IMMEDIATE, DEFERRED, BUFFERING }
}
