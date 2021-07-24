package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * TabooLib
 * taboolib.module.database.Action
 *
 * @author sky
 * @since 2021/6/23 11:54 下午
 */
interface Action {

    val query: String

    val elements: List<Any>

    fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit)

    fun runFinally(preparedStatement: PreparedStatement, connection: Connection)

}