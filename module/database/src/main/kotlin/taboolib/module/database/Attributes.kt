package taboolib.module.database

/**
 * 语句属性
 *
 * @author 坏黑
 * @since 2023/9/22 12:54
 */
interface Attributes {

    /** 数据库语句 */
    val query: String

    /** 占位符对应的元素 */
    val elements: List<Any?>
        get() = emptyList()
}