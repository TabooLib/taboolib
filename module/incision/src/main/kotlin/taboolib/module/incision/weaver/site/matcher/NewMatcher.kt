package taboolib.module.incision.weaver.site.matcher

import taboolib.module.incision.api.Anchor
import taboolib.module.incision.weaver.site.SiteSpec
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * NEW 锚点匹配器 — 对应 [SitePattern.TypeAlloc]。
 *
 * 单条 NEW 指令事件：比对 type (ASM internal name)。由于 NEW 指令没有 name/desc，
 * 只比对 ownerPattern（scanner 在 toSiteSpec 里把 NEW target 放进 ownerPattern）。
 */
class NewMatcher(sites: List<SiteSpec>) : SiteMatcher {

    override val kind: SiteMatcher.Kind = SiteMatcher.Kind.NEW

    private val relevant: List<SiteSpec> = sites.filter { it.anchor == Anchor.NEW }
    private val counters: IntArray = IntArray(relevant.size)

    override fun accepts(site: SiteSpec, pattern: SitePattern): Boolean =
        site.anchor == Anchor.NEW

    fun match(type: String): List<MatchEvent> {
        if (relevant.isEmpty()) return emptyList()
        var out: MutableList<MatchEvent>? = null
        for ((i, site) in relevant.withIndex()) {
            val ownerOk = site.ownerPattern.isEmpty() || site.ownerPattern == type
            if (!ownerOk) continue
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
