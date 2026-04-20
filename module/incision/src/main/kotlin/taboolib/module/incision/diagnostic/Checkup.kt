package taboolib.module.incision.diagnostic

import taboolib.module.incision.runtime.SurgeryRegistry

/**
 * 启动期一次性体检 — 汇总所有已注册切术，输出一张表格。
 *
 * 实际的"扫描 @Surgeon / @SurgeryDesk"工作由 TabooLib 自身的 @Awake / @Inject
 * 机制完成（object 被触达时 provideDelegate 触发注册）。本类负责在启动期、
 * advice 完成注册后做一次汇总与错误收集，不做重复扫描。
 */
object Checkup {

    /** 启动期执行：汇总所有 suture，打印一张可读表格。 */
    fun runStartupCheckup() {
        if (!Forensics.DEBUG) return
        val all = SurgeryRegistry.list()
        if (all.isEmpty()) {
            Forensics.info("Checkup: 未发现任何 incision 切术（等待 SurgeryDesk 触达）")
            return
        }
        val rows = all.map { s ->
            Row(s.id, s.targets.joinToString(",") { it.signature }, s.state.name)
        }
        val lines = buildString {
            for (r in rows) {
                appendLine("- ${r.id}")
                appendLine("  state: ${r.state}")
                appendLine("  target: ${shorten(r.target)}")
            }
        }
        Forensics.info("Checkup report: total=${all.size}\n$lines")
    }

    private fun shorten(text: String, limit: Int = 160): String {
        return if (text.length <= limit) text else text.take(limit - 1) + "…"
    }

    private data class Row(val id: String, val target: String, val state: String)
}
