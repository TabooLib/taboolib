package taboolib.module.incision.weaver.site.matcher

import taboolib.module.incision.weaver.site.pattern.InsnStep

/**
 * 单条 [InsnStep] 与一条具体指令视图的比对。
 *
 * 规则：
 *  - `opcode == -1` 表示通配，否则要求精确匹配。
 *  - 各 `*Filter` 字段空串视为不过滤；其余按 glob 规则匹配（当前只实现了 `*` 末尾通配与精确相等，
 *    后续需要更强的 glob 可在此扩展）。
 *  - `cstFilter` 与 `LdcInsn.cst.toString()` 比对。
 */
object InsnStepMatcher {

    fun matches(step: InsnStep, view: OpcodeSeqMatcher.InsnView): Boolean {
        if (step.opcode != -1 && step.opcode != view.opcode) return false
        if (!globOk(step.ownerFilter, view.owner)) return false
        if (!globOk(step.nameFilter, view.name)) return false
        if (!globOk(step.descFilter, view.descriptor)) return false
        if (step.cstFilter.isNotEmpty()) {
            val cst = view.cst ?: return false
            if (!globOk(step.cstFilter, cst.toString())) return false
        }
        return true
    }

    private fun globOk(filter: String, actual: String): Boolean {
        if (filter.isEmpty()) return true
        if (filter == "*") return true
        if (filter.endsWith("*") && !filter.startsWith("*")) {
            return actual.startsWith(filter.dropLast(1))
        }
        if (filter.startsWith("*") && !filter.endsWith("*")) {
            return actual.endsWith(filter.drop(1))
        }
        if (filter.startsWith("*") && filter.endsWith("*")) {
            return actual.contains(filter.drop(1).dropLast(1))
        }
        return filter == actual
    }
}
