package taboolib.module.incision.weaver.site.matcher

import taboolib.module.incision.api.Anchor
import taboolib.module.incision.weaver.site.SiteSpec
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * FIELD_GET / FIELD_PUT 匹配器 — 对应 [SitePattern.FieldAccess]。
 *
 * 接受 anchor 判定由 [anchor] 字段决定；SiteWeaver 创建一个 GET、一个 PUT 实例，避免共享状态。
 */
class FieldMatcher(sites: List<SiteSpec>, private val anchor: Anchor) : SiteMatcher {

    init {
        require(anchor == Anchor.FIELD_GET || anchor == Anchor.FIELD_PUT) {
            "FieldMatcher 仅支持 FIELD_GET / FIELD_PUT，实际=$anchor"
        }
    }

    override val kind: SiteMatcher.Kind = SiteMatcher.Kind.FIELD

    private val relevant: List<SiteSpec> = sites.filter { it.anchor == anchor }
    private val counters: IntArray = IntArray(relevant.size)

    override fun accepts(site: SiteSpec, pattern: SitePattern): Boolean =
        site.anchor == anchor

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
