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
        get() = Statement(joinType.name)
            .addSegment("JOIN")
            .addSegment(tableName.asFormattedColumnName())
            .addSegmentIfTrue(filter.isNotEmpty()) {
                addSegment("ON")
                addSegment(filter.query)
            }.build()

    /** 占位符对应的元素 */
    override val elements: List<Any>
        get() = filter.elements
}