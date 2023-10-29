package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * 一个行为
 *
 * @author sky
 * @since 2021/6/23 11:54 下午
 */
interface Action : Attributes {

    /**
     * 插入一段执行后回调函数，就像是：
     * ```
     * onFinally {
     *     val userId = generatedKeys.run {
     *         next()
     *         Coerce.toLong(getObject(1))
     *     }
     * ```
     */
    fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit)

    /**
     * 执行上面那个回调函数（内部用）
     */
    fun callFinally(preparedStatement: PreparedStatement, connection: Connection)
}