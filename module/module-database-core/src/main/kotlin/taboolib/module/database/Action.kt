package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * 一个行为
 *
 * @author sky
 * @since 2021/6/23 11:54 下午
 */
interface Action {

    /** 数据库语句 */
    val query: String

    /** 占位符对应的元素 */
    val elements: List<Any>

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
    fun runFinally(preparedStatement: PreparedStatement, connection: Connection)
}