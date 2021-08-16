package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * TabooLib
 * taboolib.module.database.ActionDelete
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionDelete(val table: String) : WhereExecutor(), Action {

    private var onFinally: (PreparedStatement.(Connection) -> Unit)? = null
    private var where: Where? = null

    override val query: String
        get() = "DELETE FROM ${table.formatColumn()} WHERE ${where?.query ?: ""}".trim()

    override val elements: List<Any>
        get() = where?.elements ?: emptyList()

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