package taboolib.module.incision.weaver.site.matcher

import org.objectweb.asm.Opcodes
import taboolib.module.incision.api.Anchor
import taboolib.module.incision.weaver.site.SiteSpec
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * 终结性锚点匹配器 — 对应 [SitePattern.Anywhere]：RETURN / THROW / HEAD / TAIL。
 *
 * HEAD 对应方法入口（由 SiteWeaver 在 visitCode 后发），TAIL 暂时与 RETURN 同语义（每个 return 前）。
 * THROW 对应 ATHROW 指令，RETURN 对应 IRETURN..RETURN 范围。
 */
class TerminalMatcher(sites: List<SiteSpec>) : SiteMatcher {

    override val kind: SiteMatcher.Kind = SiteMatcher.Kind.TERMINAL

    private val returnSites: List<SiteSpec> = sites.filter {
        it.anchor == Anchor.RETURN || it.anchor == Anchor.TAIL
    }
    private val throwSites: List<SiteSpec> = sites.filter { it.anchor == Anchor.THROW }
    private val headSites: List<SiteSpec> = sites.filter { it.anchor == Anchor.HEAD }

    private val returnCounters: IntArray = IntArray(returnSites.size)
    private val throwCounters: IntArray = IntArray(throwSites.size)
    private val headCounters: IntArray = IntArray(headSites.size)

    override fun accepts(site: SiteSpec, pattern: SitePattern): Boolean =
        site.anchor == Anchor.RETURN || site.anchor == Anchor.TAIL ||
            site.anchor == Anchor.THROW || site.anchor == Anchor.HEAD

    /** 指令 opcode 为 ATHROW 或 IRETURN..RETURN 时调用。 */
    fun matchInsn(opcode: Int): List<MatchEvent> = when {
        opcode == Opcodes.ATHROW -> collect(throwSites, throwCounters)
        opcode in Opcodes.IRETURN..Opcodes.RETURN -> collect(returnSites, returnCounters)
        else -> emptyList()
    }

    /** SiteWeaver 在 visitCode 之后调用一次，发出 HEAD 事件。 */
    fun matchHead(): List<MatchEvent> = collect(headSites, headCounters)

    private fun collect(sites: List<SiteSpec>, counters: IntArray): List<MatchEvent> {
        if (sites.isEmpty()) return emptyList()
        var out: MutableList<MatchEvent>? = null
        for ((i, site) in sites.withIndex()) {
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
