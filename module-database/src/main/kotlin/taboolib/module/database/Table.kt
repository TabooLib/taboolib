package taboolib.module.database

import javax.sql.DataSource

/**
 * TabooLib
 * taboolib.module.database.Table
 *
 * @author sky
 * @since 2021/6/23 11:36 上午
 */
open class Table(val name: String, vararg column: Column) {

    val columns = ArrayList<Column>()
    val primaryKeyForLegacy = ArrayList<String>()

    init {
        column(*column)
    }

    fun column(vararg column: Column) {
        columns.addAll(column)
    }

    open fun workspace(dataSource: DataSource? = null, func: Query.() -> Unit): QueryTask {
        return Query(this, dataSource).also(func).task ?: EmptyTask
    }

    override fun toString(): String {
        return "Table(name='$name', columns=$columns, primaryKeyForLegacy=$primaryKeyForLegacy)"
    }

    companion object {

        fun create(name: String, func: Table.() -> Unit = {}): Table {
            return Table(name).also(func)
        }
    }
}