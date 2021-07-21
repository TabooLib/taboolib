package taboolib.module.database

/**
 * TabooLib
 * taboolib.module.database.ActionInsert
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionInsert(val table: String, val keys: Array<String>) : QueryCallback(), Action {

    private var values = ArrayList<Array<Any>>()
    private var update = ArrayList<QuerySet>()

    override val query: String
        get() {
            var query = "INSERT INTO ${table.formatColumn()}"
            if (keys.isNotEmpty()) {
                query += " (${keys.joinToString { it.formatColumn() }})"
            }
            if (values.isNotEmpty()) {
                query += " VALUES ${values.joinToString { "(${it.joinToString { "?" }})" }}"
            }
            if (update.isNotEmpty()) {
                query += " ON DUPLICATE KEY UPDATE ${update.joinToString { it.query }}"
            }
            return query
        }

    override val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            el.addAll(values.flatMap { it.toList() })
            el.addAll(update.mapNotNull { it.value })
            return el
        }

    fun value(vararg args: Any) {
        values.add(arrayOf(*args))
    }

    fun onDuplicateKeyUpdate(func: DuplicateKey.() -> Unit) {
        update = DuplicateKey().also(func).update
    }

    class DuplicateKey {

        internal val update = ArrayList<QuerySet>()

        fun update(key: String, value: Any) {
            update += if (value is PreValue) {
                QuerySet("${key.formatColumn()} = ${value.formatColumn()}")
            } else {
                QuerySet("${key.formatColumn()} = ?", value)
            }
        }
    }

    fun pre(any: Any) = PreValue(any)
}