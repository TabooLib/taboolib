package taboolib.expansion

import taboolib.expansion.AnalyzedClassMember.Companion.toColumnName
import java.sql.Connection

/**
 * 事务上下文
 *
 * 在事务上下文中执行的所有操作共享同一个数据库连接，
 * 并在事务结束时统一提交或回滚。
 *
 * ```kotlin
 * container.transaction {
 *     val homes = get<PlayerHome>()
 *     val stats = get<PlayerStats>()
 *
 *     homes.insert(listOf(newHome))
 *     stats.update(playerStats)
 *
 *     if (someError) {
 *         rollback()  // 标记回滚
 *     }
 * }
 * ```
 *
 * @author TabooLib
 * @since 2024
 */
class TransactionContext internal constructor(
    private val container: Container<*>,
    internal val connection: Connection
) {
    private var rollbackOnly = false
    private val operators = mutableMapOf<String, ContainerOperator>()

    /**
     * 获取事务感知的操作器
     *
     * @param name 表名
     * @return 事务感知的操作器（共享 Connection）
     */
    fun operator(name: String): ContainerOperator {
        return operators.getOrPut(name) {
            val delegate = container.operator(name) as ContainerOperatorImpl
            delegate.withConnection(connection)
        }
    }

    /**
     * 获取事务感知的操作器
     *
     * @return 事务感知的操作器
     */
    inline fun <reified T> get(): ContainerOperator {
        return operator(T::class.java.simpleName.toColumnName())
    }

    /**
     * 标记事务回滚
     *
     * 调用此方法后，事务将在 block 执行完毕后回滚，
     * 所有已执行的操作都将被撤销。
     */
    fun rollback() {
        rollbackOnly = true
    }

    /**
     * 立即中止事务并回滚
     *
     * 抛出异常，立即终止事务执行并回滚所有更改。
     *
     * @param message 错误信息
     */
    fun rollbackNow(message: String = "Transaction aborted"): Nothing {
        throw TransactionAbortException(message)
    }

    /**
     * 检查事务是否应该回滚
     */
    internal fun shouldRollback() = rollbackOnly

    /**
     * 多表联查（事务内）
     *
     * 共享当前事务的 Connection，保证联查与其他操作在同一事务中。
     */
    fun join(builder: JoinQuery.() -> Unit): JoinQuery {
        return JoinQuery(container.dataSource, connection).also(builder)
    }
}

/**
 * 事务回滚异常
 *
 * 当事务被标记为回滚时抛出此异常。
 */
class TransactionRollbackException(message: String) : Exception(message)

/**
 * 事务中止异常
 *
 * 当调用 [TransactionContext.rollbackNow] 时抛出此异常。
 */
class TransactionAbortException(message: String) : Exception(message)
