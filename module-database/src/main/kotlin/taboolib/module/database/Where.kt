package taboolib.module.database

class Where {

    private val data = ArrayList<WhereData>()

    val query: String
        get() = data.joinToString(" AND ") { it.query }

    val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            fun WhereData.push() {
                if (children.isNotEmpty()) {
                    children.forEach { it.push() }
                } else {
                    el.addAll(value)
                }
            }
            data.forEach { it.push() }
            return el
        }

    infix fun String.eq(value: Any) = WhereData("${format()} = ?", listOf(value)).also {
        data += it
    }

    infix fun String.lt(value: Any) = WhereData("${format()} < ?", listOf(value)).also {
        data += it
    }

    infix fun String.lte(value: Any) = WhereData("${format()} <= ?", listOf(value)).also {
        data += it
    }

    infix fun String.gt(value: Any) = WhereData("${format()} > ?", listOf(value)).also {
        data += it
    }

    infix fun String.gte(value: Any) = WhereData("${format()} >= ?", listOf(value)).also {
        data += it
    }

    infix fun String.like(value: Any) = WhereData("${format()} LIKE ?", listOf(value)).also {
        data += it
    }

    infix fun String.inside(value: Array<String>) = WhereData("${format()} IN (${value.joinToString { "?" }})", value.toList()).also {
        data += it
    }

    infix fun String.between(value: Pair<Any, Any>) = WhereData("${format()} BETWEEN ? AND ?", listOf(value.first, value.second)).also {
        data += it
    }

    fun not(func: WhereData): WhereData {
        return func.copy(query = "NOT (${func.query})")
    }

    fun or(func: Where.() -> Unit): WhereData {
        val where = Where().also(func)
        if (where.data.isEmpty()) {
            error("empty function")
        }
        return WhereData(where.data.joinToString(" OR ") { "(${it.query})" }, children = where.data).also {
            data += it
        }
    }

    fun and(func: Where.() -> Unit): WhereData {
        val where = Where().also(func)
        if (where.data.isEmpty()) {
            error("empty function")
        }
        return WhereData(where.data.joinToString(" AND ") { "(${it.query})" }, children = where.data).also {
            data += it
        }
    }

    private fun String.format() = "`${replace(".", "`.`")}`"
}