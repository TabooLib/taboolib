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

    open fun createTable(dataSource: DataSource, checkExists: Boolean = true) {
        workspace(dataSource) { createTable(checkExists) }.run()
    }

    open fun select(dataSource: DataSource, func: ActionSelect.() -> Unit): QueryTask {
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

    open fun workspace(dataSource: DataSource, func: Query.() -> Unit): QueryTask {
        return Query(this, dataSource).also(func).tasks.lastOrNull() ?: EmptyTask
    }

    override fun toString(): String {
        return "Table(name='$name', columns=$columns, primaryKeyForLegacy=$primaryKeyForLegacy)"
    }
}