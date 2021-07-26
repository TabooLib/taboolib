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
class ActionSelect(val table: String) : WhereExecutor(), Action {

    private var onFinally: (PreparedStatement.(Connection) -> Unit)? = null
    private var distinct: String? = null
    private var rows: Array<String> = emptyArray()
    private val join = ArrayList<Join>()
    private var where: Where? = null
    private val order = ArrayList<Order>()
    private var limit = -1

    override val query: String
        get() {
            var query = "SELECT "
            query += when {
                rows.isNotEmpty() -> {
                    rows.joinToString { it.formatColumn() }
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

    override val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            el.addAll(join.flatMap { it.elements })
            el.addAll(where?.elements ?: emptyList())
            return el
        }

    fun rows(vararg row: String) {
        rows += row
    }

    fun where(whereData: WhereData) {
        if (where == null) {
            where = Where()
        }
        where!!.data += whereData
    }

    fun where(func: Where.() -> Unit) {
        if (where == null) {
            where = Where().also(func)
        } else {
            func(where!!)
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

    fun innerJoin(table: String, func: Where.() -> Unit) {
        join += Join(JoinType.INNER, table, Where().also(func))
    }

    fun leftJoin(table: String, func: Where.() -> Unit) {
        join += Join(JoinType.LEFT, table, Where().also(func))
    }

    fun rightJoin(table: String, func: Where.() -> Unit) {
        join += Join(JoinType.RIGHT, table, Where().also(func))
    }

    override fun append(whereData: WhereData) {
    }

    override fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit) {
        this.onFinally = onFinally
    }

    override fun runFinally(preparedStatement: PreparedStatement, connection: Connection) {
        this.onFinally?.invoke(preparedStatement, connection)
    }
}