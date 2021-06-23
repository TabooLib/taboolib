package taboolib.module.database

import java.util.*
import kotlin.collections.ArrayList

/**
 * TabooLib
 * taboolib.module.database.ActionSelect
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionSelect(val table: String) {

    private var rows: Array<String> = emptyArray()
    private var where: Where? = null
    private var distinct: String? = null
    private val order = ArrayList<Order>()
    private var limit = -1
    private val join = ArrayList<Join>()

    val query: String
        get() {
            var query = "SELECT "
            query += when {
                rows.isNotEmpty() -> {
                    rows.joinToString() { it.format() }
                }
                distinct != null -> {
                    "DISTINCT ${distinct!!.format()}"
                }
                else -> "*"
            }
            query += " FROM ${table.format()}"
            if (join.isNotEmpty()) {
                query += " ${join.joinToString(" ") { it.query }}"
            }
            if (where != null) {
                query += " WHERE ${where!!.query}"
            }
            if (order.isNotEmpty()) {
                query += " ORDER BY ${order.joinToString { it.query }}"
            }
            if (limit > 0) {
                query += " LIMIT $limit"
            }
            return query
        }

    fun rows(vararg row: String) {
        rows += row
    }

    fun where(func: Where.() -> Unit) {
        where = Where().also(func)
    }

    fun distinct(distinct: String) {
        this.distinct = distinct
    }

    fun order(row: String, desc: Boolean = false) {
        this.order += Order(row, desc)
    }

    fun limit(limit: Int) {
        this.limit = limit
    }

    fun innerJoin(table: String, func: Where.() -> Unit) {
        join += Join(JoinType.INNER, table, Where().also(func))
    }

    fun leftJoin(table: String, func: Where.() -> Unit) {
        join += Join(JoinType.LEFT, table, Where().also(func))
    }

    fun rightJoin(table: String, func: Where.() -> Unit) {
        join += Join(JoinType.RIGHT, table, Where().also(func))
    }

    private fun String.format() = "`${replace(".", "`.`")}`"
}