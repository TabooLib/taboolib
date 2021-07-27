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

    open fun workspace(dataSource: DataSource, func: Query.() -> Unit): QueryTask {
        return Query(this, dataSource).also(func).task ?: EmptyTask
    }

    override fun toString(): String {
        return "Table(name='$name', columns=$columns, primaryKeyForLegacy=$primaryKeyForLegacy)"
    }
}