package taboolib.module.database

/**
 * TabooLib
 * taboolib.module.database.Join
 *
 * @author sky
 * @since 2021/6/23 3:32 下午
 */
class Join(val type: JoinType, val from: String, val filter: Filter) {

    val query: String
        get() = if (filter.isEmpty()) {
            "$type JOIN `$from`"
        } else {
            "$type JOIN `$from` ON ${filter.query}"
        }

    val elements: List<Any>
        get() = filter.elements
}