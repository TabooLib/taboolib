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

    open fun execute(dataSource: DataSource? = null, func: Query.() -> Unit): Query {
        return Query(this, dataSource).also(func)
    }

    open fun executeCallback(dataSource: DataSource? = null, func: Query.() -> Unit): QueryCallback {
        return Query(this, dataSource).also(func).callback()
    }

    override fun toString(): String {
        return "Table(name='$name', columns=$columns, primaryKeyForLegacy=$primaryKeyForLegacy)"
    }

    companion object {

        fun create(name: String, func: Table.() -> Unit = {}): Table {
            return Table(name).also(func)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val table = create("name") {
                column(ColumnSQL.PRIMARY_KEY_ID)
                column(ColumnTypeSQL.VARCHAR("name", 32) {
                    options += ColumnOptionSQL.UNIQUE_KEY
                })
                column(ColumnTypeSQL.VARCHAR("data", 32) {
                    options += ColumnOptionSQL.NOTNULL
                })
            }

            table.executeCallback {
                select {
                    where {
                        "name" eq "bukkitObj"
                        or {
                            "world" eq "world"
                            "world" eq "nether"
                        }
                        not("level" between (1 to 100))
                    }
                    order("level")
                }
            }.first {
                getString("data")
            }
        }
    }
}