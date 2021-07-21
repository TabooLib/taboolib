package taboolib.module.database

/**
 * TabooLib
 * taboolib.module.database.ActionDelete
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionDelete(val table: String) : QueryCallback(), WhereExecutor, Action {

    private var where: Where? = null

    override val query: String
        get() = "DELETE FROM ${table.formatColumn()} ${where?.query ?: ""}".trim()

    override val elements: List<Any>
        get() = where?.elements ?: emptyList()

    fun where(whereData: WhereData) {
        if (where == null) {
            where = Where()
        }
        where!!.data += whereData
    }

    fun where(func: Where.() -> Unit) {
        if (where == null) {
            where = Where().also(func)
        } else {
            func(where!!)
        }
    }

    override fun append(whereData: WhereData) {
    }
}