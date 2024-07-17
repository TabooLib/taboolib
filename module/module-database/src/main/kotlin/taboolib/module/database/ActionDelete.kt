package taboolib.module.database

/**
 * 一个删除行为
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionDelete(val table: String) : ActionFilterable() {

    /** 语句 */
    override val query: String
        get() = Statement("DELETE FROM")
            .addSegment(table.asFormattedColumnName())
            .addFilter(filter)
            .build()

    /** 元素 */
    override val elements: List<Any>
        get() = filter?.elements ?: emptyList()
}