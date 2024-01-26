package taboolib.module.database

/**
 * 索引类型
 *
 * @author sky
 * @since 2021/5/9 10:47 上午
 */
enum class IndexType {

    /**
     * B+树索引
     */
    BTREE,

    /**
     * 哈希索引
     */
    HASH,

    /**
     * 默认索引
     */
    DEFAULT
}