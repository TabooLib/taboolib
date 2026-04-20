package taboolib.module.incision.weaver.site.matcher

import taboolib.module.incision.weaver.site.SiteSpec
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * 单条指令事件传入 matcher 后产出的匹配结果。
 *
 * @property siteSpec 命中的 site（保留 kind/shift/ordinal/target 供 planner/emitter 使用）。
 * @property anchorIndex 锚点指令在录制流中的序号（仅 recording 路径可用，streaming 路径留 -1）。
 *                       OpcodeSeq 类型下指向序列末尾那条指令。
 */
data class MatchEvent(
    val siteSpec: SiteSpec,
    val anchorIndex: Int = -1,
)

/**
 * 指令匹配器基础接口。按事件类型提供 `matchInvoke` / `matchField` / `matchType` / `matchTerminal`
 * 会导致接口臃肿；这里采用"上下文对象"思路，由 [taboolib.module.incision.weaver.SiteWeaver] 分类后调用对应 matcher。
 *
 * 每个实现对应一种 [SitePattern] 分支，使用滑窗时额外在 matcher 内部维护状态。
 */
interface SiteMatcher {

    /** 该 matcher 对应的 pattern 标签（用于 SiteWeaver 按 site 类型分派）。 */
    val kind: Kind

    /**
     * 判断给定 site 是否被该 matcher 负责。
     * 用于 SiteWeaver 初始化时把 sites 分桶给各 matcher。
     */
    fun accepts(site: SiteSpec, pattern: SitePattern): Boolean

    enum class Kind { INVOKE, FIELD, NEW, TERMINAL, OPCODE_SEQ }
}
