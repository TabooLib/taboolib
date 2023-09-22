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
class ActionUpdate(val table: String) : ActionFilterable() {

    /** 该行为执行完毕后的回调 */
    private var finallyCallback: (PreparedStatement.(Connection) -> Unit)? = null

    /** 操作 */
    private val operations = ArrayList<UpdateOperation>()

    /** 语句 */
    override val query: String
        get() = Statement("UPDATE")
            .addSegment(table.asFormattedColumnName())
            .addSegmentIfTrue(operations.isNotEmpty()) {
                addSegment("SET")
                addOperations(operations)
            }
            .addFilter(filter)
            .build()

    /** 元素 */
    override val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            el.addAll(operations.mapNotNull { it.value })
            el.addAll(filter?.elements ?: emptyList())
            return el
        }

    /** 设置 */
    fun set(key: String, value: Any?) {
        operations += when (value) {
            null -> UpdateOperation("${key.asFormattedColumnName()} = null")
            is PreValue -> UpdateOperation("${key.asFormattedColumnName()} = ${value.asFormattedColumnName()}")
            else -> UpdateOperation("${key.asFormattedColumnName()} = ?", value)
        }
    }

    override fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit) {
        this.finallyCallback = onFinally
    }

    override fun callFinally(preparedStatement: PreparedStatement, connection: Connection) {
        this.finallyCallback?.invoke(preparedStatement, connection)
    }
}