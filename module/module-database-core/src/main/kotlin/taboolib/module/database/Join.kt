package taboolib.module.database

/**
 * Join 语法中所包含的信息
 *
 * @author sky
 * @since 2021/6/23 3:32 下午
 */
class Join(val joinType: JoinType, val tableName: String, val filter: Filter) : Attributes {

    /** 语句 */
    override val query: String
        get() = if (filter.isEmpty()) {
            "$joinType JOIN ${tableName.asFormattedColumnName()}"
        } else {
            "$joinType JOIN ${tableName.asFormattedColumnName()} ON ${filter.query}"
        }

    /** 占位符对应的元素 */
    override val elements: List<Any>
        get() = filter.elements
}