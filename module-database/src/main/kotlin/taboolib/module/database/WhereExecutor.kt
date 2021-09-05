package taboolib.module.database

/**
 * TabooLib
 * taboolib.module.database.WhereExecutor
 *
 * @author sky
 * @since 2021/6/24 1:58 上午
 */
abstract class WhereExecutor {

    abstract fun append(whereData: WhereData)

    infix fun String.eq(value: Any): WhereData {
        return if (value is PreValue) {
            WhereData("${formatColumn()} = ${value.formatColumn()}").also {
                append(it)
            }
        } else {
            WhereData("${formatColumn()} = ?", listOf(value)).also {
                append(it)
            }
        }
    }

    infix fun String.lt(value: Any): WhereData {
        return if (value is PreValue) {
            WhereData("${formatColumn()} < ${value.formatColumn()}").also {
                append(it)
            }
        } else {
            WhereData("${formatColumn()} < ?", listOf(value)).also {
                append(it)
            }
        }
    }

    infix fun String.lte(value: Any): WhereData {
        return if (value is PreValue) {
            WhereData("${formatColumn()} <= ${value.formatColumn()}").also {
                append(it)
            }
        } else {
            WhereData("${formatColumn()} <= ?", listOf(value)).also {
                append(it)
            }
        }
    }

    infix fun String.gt(value: Any): WhereData {
        return if (value is PreValue) {
            WhereData("${formatColumn()} > ${value.formatColumn()}").also {
                append(it)
            }
        } else {
            WhereData("${formatColumn()} > ?", listOf(value)).also {
                append(it)
            }
        }
    }

    infix fun String.gte(value: Any): WhereData {
        return if (value is PreValue) {
            WhereData("${formatColumn()} >= ${value.formatColumn()}").also {
                append(it)
            }
        } else {
            WhereData("${formatColumn()} >= ?", listOf(value)).also {
                append(it)
            }
        }
    }

    infix fun String.like(value: Any): WhereData {
        return if (value is PreValue) {
            WhereData("${formatColumn()} LIKE ${value.formatColumn()}").also {
                append(it)
            }
        } else {
            WhereData("${formatColumn()} LIKE ?", listOf(value)).also {
                append(it)
            }
        }
    }

    infix fun String.inside(value: Array<String>) = WhereData("${formatColumn()} IN (${value.joinToString { "?" }})", value.toList()).also {
        append(it)
    }

    infix fun String.between(value: Pair<Any, Any>) = WhereData("${formatColumn()} BETWEEN ? AND ?", listOf(value.first, value.second)).also {
        append(it)
    }

    infix fun WhereData.or(other: WhereData): WhereData {
        return WhereData("(${query} OR ${other.query})", children = listOf(this, other))
    }

    infix fun WhereData.and(other: WhereData): WhereData {
        return WhereData("(${query} AND ${other.query})", children = listOf(this, other))
    }

    fun not(func: WhereData): WhereData {
        return func.copy(query = "NOT (${func.query})")
    }

    fun or(func: Where.() -> Unit): WhereData {
        val where = Where().also(func)
        if (where.data.isEmpty()) {
            error("empty function")
        }
        return WhereData("(${where.data.joinToString(" OR ") { it.query }})", children = where.data).also {
            append(it)
        }
    }

    fun and(func: Where.() -> Unit): WhereData {
        val where = Where().also(func)
        if (where.data.isEmpty()) {
            error("empty function")
        }
        return WhereData("(${where.data.joinToString(" AND ") { it.query }})", children = where.data).also {
            append(it)
        }
    }

    fun pre(any: Any): PreValue {
        return PreValue(any)
    }
}