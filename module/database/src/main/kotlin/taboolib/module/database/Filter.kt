package taboolib.module.database

/**
 * 过滤函数中的上下文环境
 *
 * @author sky
 * @since 2021/6/23 11:47 上午
 */
open class Filter : Filterable(), Attributes {

    /** 过滤标准 */
    val criteria = ArrayList<Criteria>()

    /** 语句 */
    override val query: String
        get() = criteria.joinToString(" AND ") { it.query }

    /** 元素 */
    override val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            fun Criteria.push() {
                if (children.isNotEmpty()) {
                    children.forEach { it.push() }
                } else {
                    el.addAll(value)
                }
            }
            criteria.forEach { it.push() }
            return el
        }

    /** 追加一个过滤标准 */
    override fun append(criteria: Criteria) {
        this.criteria += criteria
    }

    override fun remove(vararg criteria: Criteria) {
        this.criteria -= criteria.toSet()
    }

    /** 过滤标准是否为空 */
    fun isEmpty(): Boolean {
        return criteria.isEmpty()
    }

    /** 过滤标准是否不为空 */
    fun isNotEmpty(): Boolean {
        return criteria.isNotEmpty()
    }
}