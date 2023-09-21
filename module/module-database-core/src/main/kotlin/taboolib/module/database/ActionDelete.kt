package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * 一个删除行为
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionDelete(val table: String) : Filterable(), Action {

    private var onFinally: (PreparedStatement.(Connection) -> Unit)? = null
    private var filter: Filter? = null

    override val query: String
        get() = "DELETE FROM ${table.formatColumn()} WHERE ${filter?.query ?: ""}".trim()

    override val elements: List<Any>
        get() = filter?.elements ?: emptyList()

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