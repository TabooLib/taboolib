package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * TabooLib
 * taboolib.module.database.ActionSelect
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionSelect(val table: String) : Filterable(), Action {

    private var onFinally: (PreparedStatement.(Connection) -> Unit)? = null
    private var distinct: String? = null
    private var rows: Array<String> = emptyArray()
    private val join = ArrayList<Join>()
    private var filter: Filter? = null
    private val order = ArrayList<Order>()
    private var limit = -1

    override val query: String
        get() {
            var query = "SELECT "
            query += when {
                rows.isNotEmpty() -> {
                    rows.joinToString { if (it.contains('(') && it.endsWith(')')) it else it.formatColumn() }
                }
                distinct != null -> {
                    "DISTINCT ${distinct!!.formatColumn()}"
                }
                else -> "*"
            }
            query += " FROM ${table.formatColumn()}"
            if (join.isNotEmpty()) {
                query += " ${join.joinToString(" ") { it.query }}"
            }
            if (filter != null && !filter!!.isEmpty()) {
                query += " WHERE ${filter!!.query}"
            }
            if (order.isNotEmpty()) {
                query += " ORDER BY ${order.joinToString { it.query }}"
            }
            if (limit > 0) {
                query += " LIMIT $limit"
            }
            return query
        }

    override val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            el.addAll(join.flatMap { it.elements })
            el.addAll(filter?.elements ?: emptyList())
            return el
        }

    fun rows(vararg row: String) {
        if (row.isNotEmpty()) {
            rows += row
        }
    }

    fun where(filterCriteria: Criteria) {
        if (filter == null) {
            filter = Filter()
        }
        filter!!.data += filterCriteria
    }

    fun where(func: Filter.() -> Unit) {
        if (filter == null) {
            filter = Filter().also(func)
        } else {
            func(filter!!)
        }
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

    fun innerJoin(table: String, func: Filter.() -> Unit) {
        join += Join(JoinType.INNER, table, Filter().also(func))
    }

    fun leftJoin(table: String, func: Filter.() -> Unit) {
        join += Join(JoinType.LEFT, table, Filter().also(func))
    }

    fun rightJoin(table: String, func: Filter.() -> Unit) {
        join += Join(JoinType.RIGHT, table, Filter().also(func))
    }

    override fun append(criteria: Criteria) {
    }

    override fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit) {
        this.onFinally = onFinally
    }

    override fun runFinally(preparedStatement: PreparedStatement, connection: Connection) {
        this.onFinally?.invoke(preparedStatement, connection)
    }
}