package taboolib.module.incision.runtime

import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.api.Theatre
import taboolib.module.incision.pred.Predicate
import taboolib.module.incision.weaver.site.SiteSpec

/**
 * Advice 类型。
 */
enum class AdviceKind { LEAD, TRAIL, SPLICE, GRAFT, BYPASS, TRIM, EXCISE }

/**
 * 运行时单条 advice 记录 — dispatcher 表中的条目。
 */
data class AdviceEntry(
    val id: String,
    val kind: AdviceKind,
    val target: MethodCoordinate,
    val priority: Int,
    val handler: (Theatre) -> Any?,
    val predicate: ((Theatre) -> Boolean)? = null,
    /**
     * 编译后的字符串谓词（来自 DSL 的 `where("...")` 字面量）。
     * 与 [predicate] 互不影响：dispatcher 先 test 此字段，再 test 闭包 [predicate]。
     * 解析与编译在 advice 注册期完成；运行期零反射、零 AST 遍历。
     */
    val compiledPredicate: Predicate? = null,
    /** [compiledPredicate] 的源码，仅用于诊断输出。 */
    val predicateSource: String? = null,
    val pluginName: String = "",
    val classLoader: java.lang.ref.WeakReference<ClassLoader>? = null,
    val explicitResumeRequired: Boolean = false,
    val sourceKind: String = "dsl",
    val aliasGroup: String? = null,
    val siteSpec: SiteSpec? = null,
    /** TRAIL 是否在异常出口 (ATHROW) 触发；默认 true。对非 TRAIL 无效。 */
    val onThrow: Boolean = true,
    @Volatile var enabled: Boolean = true,
)

/**
 * 单个目标方法的 advice 链 — 按优先级降序，同优先级按注册顺序。
 */
class AdviceChain(val target: MethodCoordinate) {

    private val entries = java.util.concurrent.CopyOnWriteArrayList<AdviceEntry>()

    fun add(entry: AdviceEntry) {
        entries.add(entry)
        entries.sortByDescending { it.priority }
    }

    fun remove(id: String): Boolean = entries.removeIf { it.id == id }

    fun removeByClassLoader(cl: ClassLoader): Int {
        var n = 0
        entries.removeIf { e ->
            val held = e.classLoader?.get()
            if (held === cl) { n++; true } else false
        }
        return n
    }

    fun list(): List<AdviceEntry> = entries.toList()

    fun isEmpty(): Boolean = entries.isEmpty()
}
