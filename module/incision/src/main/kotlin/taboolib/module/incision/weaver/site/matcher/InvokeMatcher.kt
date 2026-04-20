package taboolib.module.incision.weaver.site.matcher

import taboolib.module.incision.api.Anchor
import taboolib.module.incision.weaver.site.SiteSpec
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * INVOKE 锚点匹配器 — 对应 [SitePattern.MethodCall]。
 *
 * 单条 INVOKE 指令事件：逐 site 比对 owner/name/desc（空串视为通配），推进 ordinal 计数器，
 * 当 ordinal 为 -1（全部）或等于当前计数时发出 [MatchEvent]。
 *
 * 与原 SiteWeaver.SiteVisitor.matchFor 保持语义一致，便于 streaming / recording 双路径复用。
 */
class InvokeMatcher(sites: List<SiteSpec>) : SiteMatcher {

    override val kind: SiteMatcher.Kind = SiteMatcher.Kind.INVOKE

    private val relevant: List<SiteSpec> = sites.filter { it.anchor == Anchor.INVOKE }
    private val counters: IntArray = IntArray(relevant.size)

    override fun accepts(site: SiteSpec, pattern: SitePattern): Boolean =
        site.anchor == Anchor.INVOKE

    fun match(owner: String, name: String, descriptor: String): List<MatchEvent> {
        if (relevant.isEmpty()) return emptyList()
        var out: MutableList<MatchEvent>? = null
        for ((i, site) in relevant.withIndex()) {
            val ownerOk = site.ownerPattern.isEmpty() || site.ownerPattern == owner
            val nameOk = site.namePattern.isEmpty() || site.namePattern == name
            val descOk = site.descPattern.isEmpty() || site.descPattern == descriptor
            if (!ownerOk || !nameOk || !descOk) continue
            val idx = counters[i]
            counters[i] = idx + 1
            if (site.ordinal < 0 || site.ordinal == idx) {
                if (out == null) out = mutableListOf()
                out.add(MatchEvent(site))
            }
        }
        return out ?: emptyList()
    }
}
