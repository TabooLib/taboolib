package taboolib.module.database

/**
 * TabooLib
 * taboolib.module.database.ActionUpdate
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionUpdate(val table: String) : QueryHook(), WhereExecutor, Action {

    private val set = ArrayList<SetData>()
    private var where: Where? = null

    override val query: String
        get() {
            var query = "UPDATE ${table.formatColumn()}"
            if (set.isNotEmpty()) {
                query += " SET ${set.joinToString { it.query }}"
            }
            if (where != null) {
                query += " WHERE ${where!!.query}"
            }
            return query
        }

    override val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            el.addAll(set.mapNotNull { it.value })
            el.addAll(where?.elements ?: emptyList())
            return el
        }

    fun set(key: String, value: Any) {
        set += if (value is PreValue) {
            SetData("${key.formatColumn()} = ${value.formatColumn()}")
        } else {
            SetData("${key.formatColumn()} = ?", value)
        }
    }

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