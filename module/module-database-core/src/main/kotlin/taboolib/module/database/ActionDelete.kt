package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * 一个删除行为
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionDelete(val table: String) : ActionFilterable() {

    /** 该行为执行完毕后的回调 */
    private var finallyCallback: (PreparedStatement.(Connection) -> Unit)? = null

    /** 语句 */
    override val query: String
        get() = Statement("DELETE FROM")
            .addSegment(table.asFormattedColumnName())
            .addFilter(filter)
            .build()

    /** 元素 */
    override val elements: List<Any>
        get() = filter?.elements ?: emptyList()

    override fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit) {
        this.finallyCallback = onFinally
    }

    override fun callFinally(preparedStatement: PreparedStatement, connection: Connection) {
        this.finallyCallback?.invoke(preparedStatement, connection)
    }
}