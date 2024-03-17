package taboolib.module.database

import javax.sql.DataSource

/**
 * TabooLib
 * taboolib.module.database.Table
 *
 * @author sky
 * @since 2021/6/23 11:36 上午
 */
@Suppress("LeakingThis")
open class Table<T : Host<E>, E : ColumnBuilder>(val name: String, val host: Host<E>, func: Table<T, E>.() -> Unit = {}) {

    val columns = ArrayList<Column>()
    val primaryKeyForLegacy = ArrayList<String>()

    val indices = ArrayList<Index>()

    init {
        func(this)
    }

    @Suppress("UNCHECKED_CAST")
    open fun add(name: String? = null, func: E.() -> Unit): Table<T, E> {
        val builder = host.columnBuilder
        builder.name = name
        func(builder as E)
        columns += builder.getColumn()
        return this
    }

    open fun index(name: String, columns: List<String>, unique: Boolean = false, checkExists: Boolean = true): Table<T, E> {
        indices += Index(name, columns, unique, checkExists)
        return this
    }

    open fun createTable(dataSource: DataSource, checkExists: Boolean = true) {
        workspace(dataSource) { createTable(checkExists) }.run()
    }

    open fun createIndex(dataSource: DataSource, name: String, columns: List<String>, unique: Boolean = false, checkExists: Boolean = true) {
        workspace(dataSource) { createIndex(Index(name, columns, unique, checkExists)) }.run()
    }

    open fun select(dataSource: DataSource, func: ActionSelect.() -> Unit): ResultProcessorList {
        return workspace(dataSource) { select(func) }
    }

    open fun find(dataSource: DataSource, func: ActionSelect.() -> Unit): Boolean {
        return workspace(dataSource) { select(func) }.find()
    }

    open fun update(dataSource: DataSource, func: ActionUpdate.() -> Unit): Int {
        return workspace(dataSource) { update(func) }.run()
    }

    open fun delete(dataSource: DataSource, func: ActionDelete.() -> Unit): Int {
        return workspace(dataSource) { delete(func) }.run()
    }

    open fun insert(dataSource: DataSource, vararg keys: String, func: ActionInsert.() -> Unit): Int {
        return workspace(dataSource) { insert(*keys) { func(this) } }.run()
    }

    open fun insert(dataSource: DataSource, keys: List<String>, func: ActionInsert.() -> Unit): Int {
        return workspace(dataSource) { insert(keys) { func(this) } }.run()
    }

    /**
     * # 创建工作空间
     *
     * 以一个连接的方式执行多个操作
     *
     * ```
     * workspace(dataSource) {
     *     update {
     *          set("data", 1)
     *          where("name" eq "sky")
     *     }
     *     update {
     *          set("data", 2)
     *          where("name" eq "black")
     *     }
     * }.run()
     * ```
     *
     * 最终你需要一个执行函数（`run()` 或 `find()` 等）来完成这些操作。
     *
     * ```
     * workspace(dataSource) {
     *     update {
     *          set("data", 1)
     *          where("name" eq "sky")
     *     }
     *     select {
     *         where("name" eq "sky")
     *     }.first {
     *         info("sky's data is ${getInteger("data")}")
     *     }
     * }.run()
     * ```
     *
     * 需要注意的是，上面工作空间中的 `select` 操作只会执行一次，且在 `run()` 之前。
     */
    open fun workspace(dataSource: DataSource, func: ExecutableSource.() -> Unit): ResultProcessorList {
        val source = ExecutableSource(this, dataSource, false).also(func)
        return ResultProcessorList(source.processors, source)
    }

    /**
     * # 创建事务空间
     *
     * 在数据库中，事务指的是一组作为单个工作单位执行的操作，这些操作要么全都完成，要么全都不完成。
     * 事务为数据库操作提供了一种机制，确保数据始终保持一致状态。
     * 如果事务中的某个操作失败，那么整个事务将被回滚，所有已完成的操作都将撤销。
     *
     * ```
     * val isSuccess = transaction(dataSource) {
     *     update {
     *          set("data", 1)
     *          where("name" eq "sky")
     *     }
     *     update {
     *          set("data", 2)
     *          where("name" eq "black")
     *     }
     * }.isSuccess
     * ```
     *
     * 与 `workspace` 不同的是，`transaction` 会自动执行并提交事务，不需要调用执行函数 `run()`。
     */
    open fun transaction(dataSource: DataSource, func: ExecutableSource.() -> Unit): Result<Unit> {
        return ExecutableSource(this, dataSource, true).also(func).saveChanges()
    }

    override fun toString(): String {
        return "Table(name='$name', columns=$columns, primaryKeyForLegacy=$primaryKeyForLegacy)"
    }
}
