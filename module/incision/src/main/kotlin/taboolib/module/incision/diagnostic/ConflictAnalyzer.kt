package taboolib.module.incision.diagnostic

import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.runtime.AdviceEntry
import taboolib.module.incision.runtime.AdviceKind
import taboolib.module.incision.runtime.SurgeryRegistry
import taboolib.module.incision.runtime.TheatreDispatcher

/**
 * 冲突分析器 — 在每次 register 时跑一遍，检查同一目标的 advice 组合是否存在已知风险。
 *
 * 规则：
 * - **同一 target 上有 ≥2 个 [AdviceKind.EXCISE]** → 强制 [Trauma.Conflict.MultipleExcise]
 * - **EXCISE + 任意非 LEAD/TRAIL** → 警告（链中后续 advice 可能不可达）
 * - **多个 BYPASS** → 警告（链式 resume 行为依赖 priority 顺序，易出错）
 *
 * Lead/Trail/Splice 多对一不警告；它们设计上就支持叠加。
 */
object ConflictAnalyzer {

    data class Report(val target: MethodCoordinate, val severity: Severity, val message: String, val involved: List<AdviceEntry>)

    enum class Severity { ERROR, WARN, INFO }

    /** 分析当前已注册 chain，返回所有报告。 */
    fun analyze(target: MethodCoordinate): List<Report> {
        val entries = TheatreDispatcher.chainOf(target).list()
        if (entries.size < 2) return emptyList()
        val reports = mutableListOf<Report>()
        val excises = entries.filter { it.kind == AdviceKind.EXCISE }
        val bypasses = entries.filter { it.kind == AdviceKind.BYPASS }
        if (excises.size > 1) {
            reports += Report(target, Severity.ERROR, "同一 target 有 ${excises.size} 个 @Excise — 仅允许一个", excises)
        }
        if (excises.isNotEmpty() && entries.size > excises.size) {
            val others = entries.filter { it.kind !in setOf(AdviceKind.LEAD, AdviceKind.TRAIL, AdviceKind.EXCISE) }
            if (others.isNotEmpty()) {
                reports += Report(target, Severity.WARN, "@Excise 与 ${others.joinToString { it.kind.name }} 共存，后者可能不可达", others + excises)
            }
        }
        if (bypasses.size > 1) {
            reports += Report(target, Severity.WARN, "同一 target 有 ${bypasses.size} 个 @Bypass，链式调用可能产生循环或顺序歧义", bypasses)
        }
        return reports
    }

    /** 全量分析 — 对所有 target 跑一遍 */
    fun analyzeAll(): List<Report> {
        val all = mutableListOf<Report>()
        SurgeryRegistry.list().flatMap { it.targets }.distinct().forEach { all += analyze(it) }
        return all
    }

    /** 出错 → 抛 Trauma；警告 → Forensics.warn；info → Forensics.info */
    fun emit(report: Report) {
        when (report.severity) {
            Severity.ERROR -> {
                if (report.message.contains("Excise")) {
                    throw Trauma.Conflict.MultipleExcise(report.target, report.involved.map { it.id })
                }
                Forensics.error("[Conflict] ${report.target} ${report.message}")
            }
            Severity.WARN -> {
                if (report.message.contains("Bypass")) {
                    Forensics.warn("[Conflict] ${report.target} ${report.message} ids=${report.involved.map { it.id }}")
                } else {
                    Forensics.warn("[Conflict] ${report.target} ${report.message} ids=${report.involved.map { it.id }}")
                }
            }
            Severity.INFO -> Forensics.info("[Conflict] ${report.target} ${report.message}")
        }
    }
}
