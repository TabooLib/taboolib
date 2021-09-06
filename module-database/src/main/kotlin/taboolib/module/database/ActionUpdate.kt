package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * TabooLib
 * taboolib.module.database.ActionUpdate
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionUpdate(val table: String) : WhereExecutor(), Action {

    private var onFinally: (PreparedStatement.(Connection) -> Unit)? = null
    private val set = ArrayList<QuerySet>()
    private var where: Where? = null

    override val query: String
        get() {
            var query = "UPDATE ${table.formatColumn()}"
            if (set.isNotEmpty()) {
                query += " SET ${set.joinToString { it.query }}"
            }
            if (where != null) {
                query += " WHERE ${where!!.query}"
            }
            return query
        }

    override val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            el.addAll(set.mapNotNull { it.value })
            el.addAll(where?.elements ?: emptyList())
            return el
        }

    fun set(key: String, value: Any?) {
        set += when (value) {
            null -> {
                QuerySet("${key.formatColumn()} = null")
            }
            is PreValue -> {
                QuerySet("${key.formatColumn()} = ${value.formatColumn()}")
            }
            else -> {
                QuerySet("${key.formatColumn()} = ?", value)
            }
        }
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

    override fun append(whereData: WhereData) {
    }

    override fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit) {
        this.onFinally = onFinally
    }

    override fun runFinally(preparedStatement: PreparedStatement, connection: Connection) {
        this.onFinally?.invoke(preparedStatement, connection)
    }
}