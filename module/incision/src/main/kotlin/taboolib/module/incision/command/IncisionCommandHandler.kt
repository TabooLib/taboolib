package taboolib.module.incision.command

import taboolib.module.incision.dsl.Scalpel
import taboolib.module.incision.runtime.SurgeryRegistry
import taboolib.module.incision.runtime.TheatreDispatcher

/**
 * 不强依赖 TabooLib 命令系统的"命令处理器"。
 *
 * 调用方在自己插件里挂 TabooLib `@CommandHeader`，把子命令路由到本对象的方法。
 * 这样 incision 模块本身仍 platform-agnostic。
 *
 * 子命令：
 * - `list`   列出所有切术
 * - `show <id>`  打印某切术详情
 * - `heal <id>`  卸载该切术
 * - `dump`   打印 dispatcher 链快照
 * - `plugins` 按 pluginName 分组展示 advice
 */
object IncisionCommandHandler {

    fun list(): String = buildString {
        val all = SurgeryRegistry.list()
        appendLine("[Incision] sutures (${all.size}):")
        for (s in all) {
            appendLine("  ${s.id} state=${s.state} targets=${s.targets.size}")
        }
    }

    fun show(id: String): String {
        val s = Scalpel.find(id) ?: return "[Incision] not found: $id"
        return buildString {
            appendLine("[Incision] $id")
            appendLine("  state : ${s.state}")
            appendLine("  holder: ${s.holder.qualifiedName}")
            for (t in s.targets) {
                appendLine("  target: ${t.signature}")
                val chain = TheatreDispatcher.chainOf(t).list()
                for (e in chain) {
                    appendLine("    advice id=${e.id} kind=${e.kind} pri=${e.priority} enabled=${e.enabled}")
                }
            }
        }
    }

    fun heal(id: String): String {
        val s = Scalpel.find(id) ?: return "[Incision] not found: $id"
        return if (s.heal()) "[Incision] healed $id" else "[Incision] heal failed (already healed?)"
    }

    fun dump(): String = buildString {
        appendLine("[Incision] dispatcher dump:")
        for (s in SurgeryRegistry.list()) {
            for (t in s.targets) {
                val chain = TheatreDispatcher.chainOf(t).list()
                if (chain.isEmpty()) continue
                appendLine("  ${t.signature} (${chain.size} advice)")
                for (e in chain) appendLine("    - ${e.id} kind=${e.kind} pri=${e.priority}")
            }
        }
    }

    fun plugins(): String = buildString {
        val byPlugin = SurgeryRegistry.list().groupBy { it.id.substringBefore('#').substringBefore('.', "?") }
        appendLine("[Incision] sutures by holder package (${byPlugin.size}):")
        for ((k, v) in byPlugin) {
            appendLine("  $k: ${v.size}")
            for (s in v) appendLine("    - ${s.id}")
        }
    }
}
