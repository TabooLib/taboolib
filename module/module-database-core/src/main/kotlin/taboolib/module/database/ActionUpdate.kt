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
class ActionUpdate(val table: String) : Filterable(), Action {

    private var onFinally: (PreparedStatement.(Connection) -> Unit)? = null
    private val set = ArrayList<QuerySet>()
    private var filter: Filter? = null

    override val query: String
        get() {
            var query = "UPDATE ${table.formatColumn()}"
            if (set.isNotEmpty()) {
                query += " SET ${set.joinToString { it.query }}"
            }
            if (filter != null) {
                query += " WHERE ${filter!!.query}"
            }
            return query
        }

    override val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            el.addAll(set.mapNotNull { it.value })
            el.addAll(filter?.elements ?: emptyList())
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

    override fun append(criteria: Criteria) {
    }

    override fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit) {
        this.onFinally = onFinally
    }

    override fun runFinally(preparedStatement: PreparedStatement, connection: Connection) {
        this.onFinally?.invoke(preparedStatement, connection)
    }
}