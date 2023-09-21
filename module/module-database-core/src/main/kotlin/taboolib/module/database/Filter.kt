package taboolib.module.database

/**
 * 过滤函数中的上下文环境
 *
 * @author sky
 * @since 2021/6/23 11:47 上午
 */
class Filter : Filterable() {

    internal val data = ArrayList<Criteria>()

    val query: String
        get() = data.joinToString(" AND ") { it.query }

    val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            fun Criteria.push() {
                if (children.isNotEmpty()) {
                    children.forEach { it.push() }
                } else {
                    el.addAll(value)
                }
            }
            data.forEach { it.push() }
            return el
        }

    fun isEmpty(): Boolean {
        return data.isEmpty()
    }

    override fun append(criteria: Criteria) {
        data += criteria
    }
}