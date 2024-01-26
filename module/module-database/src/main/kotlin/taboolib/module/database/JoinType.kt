package taboolib.module.database

/**
 * 连接类型
 *
 * @author sky
 * @since 2021/6/23 3:15 下午
 */
enum class JoinType {

    /**
     * 左连接（左表的每一行都会加上右表中符合条件的行）
     */
    LEFT,

    /**
     * 右连接（右表的每一行都会加上左表中符合条件的行）
     */
    RIGHT,

    /**
     * 内连接（两表的交集）
     */
    INNER
}