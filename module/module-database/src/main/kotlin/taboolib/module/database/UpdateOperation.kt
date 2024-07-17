package taboolib.module.database

/**
 * 更新操作
 *
 * @author sky
 * @since 2021/6/24 1:23 上午
 */
data class UpdateOperation(
    // 语句（如：`name` = ?）
    override val query: String,
    // 元素值
    val value: Any? = null
) : Attributes