package taboolib.expansion.mapper

import taboolib.expansion.*
import taboolib.module.database.Filter
import java.sql.Connection

/**
 * 事务内的 DataMapper 实现
 *
 * 使用事务感知的 ContainerOperator，所有操作共享同一个数据库连接。
 * 不支持嵌套事务，调用 transaction() 会抛出异常。
 *
 * 事务内不使用缓存读取（保证事务一致性），但写入操作会失效外层 L2 缓存。
 */
class TransactionalDataMapper<T>(
    override val type: Class<T>,
    override val operator: ContainerOperator,
    override val cache: L2Cache?,
    private val sharedConnection: Connection? = null
) : AbstractDataMapper<T>() {

    // 事务内不走缓存，executeRead/executeBeanRead 使用基类默认实现（直接执行）

    // === 游标查询（事务内支持）===

    override fun selectCursor(filter: Filter.() -> Unit): Cursor<T> {
        return operator.selectCursor(type, filter)
    }

    override fun sortCursor(row: String, filter: Filter.() -> Unit): Cursor<T> {
        return operator.sortCursor(type, row, filter)
    }

    override fun sortDescendingCursor(row: String, filter: Filter.() -> Unit): Cursor<T> {
        return operator.sortDescendingCursor(type, row, filter)
    }

    // === 事务 ===

    override fun <R> transaction(block: DataMapper<T>.() -> R): Result<R> {
        error("Nested transactions are not supported")
    }

    // === 多表联查 ===

    override fun join(builder: JoinQuery.() -> Unit): JoinQuery {
        return JoinQuery(operator.dataSource, sharedConnection).also {
            it.from(tableName)
            builder(it)
        }
    }

    // === 生命周期 ===

    override fun close() {
        error("Cannot close container within a transaction")
    }
}
