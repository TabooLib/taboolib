package taboolib.module.incision.weaver.site.matcher

import taboolib.module.incision.weaver.site.SiteSpec
import taboolib.module.incision.weaver.site.pattern.InsnStep
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * OpcodeSeq 匹配器 — 对应 [SitePattern.OpcodeSeq]。
 *
 * 使用滑动窗口，在流经的指令序列上尝试匹配 [InsnStep] 列表。当全部步骤按顺序命中后，
 * 产出一条 [MatchEvent]，其中 `anchorIndex` 指向序列末尾那条指令（录制流索引）。
 *
 * 当前只提供骨架：步骤比对下沉到 [InsnStepMatcher]；具体的 opcode/owner/name/desc/cst
 * 比对在那里实现。真正的窗口推进在 recording 路径由 [taboolib.module.incision.weaver.SiteWeaver]
 * 在 Replayer 回放前调用 [match] 完成。
 *
 * 注意：streaming 路径不支持 OpcodeSeq（因为需要向前看连续 N 条指令），SiteWeaver 遇到此类 site
 * 自动切换到 recording 路径。
 */
class OpcodeSeqMatcher(private val entries: List<Entry>) : SiteMatcher {

    override val kind: SiteMatcher.Kind = SiteMatcher.Kind.OPCODE_SEQ

    override fun accepts(site: SiteSpec, pattern: SitePattern): Boolean =
        pattern is SitePattern.OpcodeSeq

    /**
     * 在已录制的指令流 [events] 上搜索所有命中。
     *
     * @param events 描述每条指令关键信息（opcode/owner/name/desc/cst）的序列。索引与录制流一致。
     * @return 所有命中事件，anchorIndex 指向命中序列的最后一条指令在录制流中的索引。
     */
    fun match(events: List<InsnView>): List<MatchEvent> {
        if (entries.isEmpty() || events.isEmpty()) return emptyList()
        val out = ArrayList<MatchEvent>()
        for (entry in entries) {
            val seq = entry.steps
            if (seq.isEmpty()) continue
            // 滑窗：在 events 上尝试从每个位置起匹配整个序列
            var matchedOrdinal = 0
            var i = 0
            while (i < events.size) {
                val end = tryMatchAt(events, i, seq)
                if (end >= 0) {
                    if (entry.site.ordinal < 0 || entry.site.ordinal == matchedOrdinal) {
                        out += MatchEvent(entry.site, anchorIndex = end)
                    }
                    matchedOrdinal++
                    i = end + 1
                } else {
                    i++
                }
            }
        }
        return out
    }

    /**
     * 从 events[start] 开始尝试匹配整个 seq；成功返回命中末尾索引，失败返回 -1。
     *
     * 语义是“以 start 作为首 step 的锚点，后续 step 允许向前跳过不相关指令”，
     * 而不是“所有 step 必须严格相邻”。
     *
     * 这样才能覆盖：
     * - `LDC;LDC;INVOKEVIRTUAL` 之间夹着 ASTORE / NEW / DUP / ALOAD
     * - `PUTFIELD repeat=2/5`、`ARRAYLENGTH repeat=2` 之间夹着装载与常量指令
     */
    private fun tryMatchAt(events: List<InsnView>, start: Int, seq: List<InsnStep>): Int {
        if (start >= events.size || !InsnStepMatcher.matches(seq.first(), events[start])) return -1
        var cursor = start + 1
        var end = start
        for ((stepIndex, step) in seq.withIndex()) {
            val repeat = step.repeat.coerceAtLeast(1)
            repeat(repeat) { repeatIndex ->
                if (stepIndex == 0 && repeatIndex == 0) {
                    end = start
                    return@repeat
                }
                var matchedAt = -1
                while (cursor < events.size) {
                    if (InsnStepMatcher.matches(step, events[cursor])) {
                        matchedAt = cursor
                        cursor++
                        break
                    }
                    cursor++
                }
                if (matchedAt < 0) return -1
                end = matchedAt
            }
        }
        return end
    }

    /** 录制流里单条指令的"视图"——给匹配器使用的最小可比对信息。 */
    data class InsnView(
        val opcode: Int,
        val owner: String = "",
        val name: String = "",
        val descriptor: String = "",
        val cst: Any? = null,
    )

    /** 组合 OpcodeSeq site 与其 steps。SiteWeaver 构建时从 site 对应的 SitePattern.OpcodeSeq 拆出 steps。 */
    data class Entry(val site: SiteSpec, val steps: List<InsnStep>)
}
