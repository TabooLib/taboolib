package taboolib.module.database

/**
 * 排序
 *
 * @author sky
 * @since 2019-10-26 14:02
 */
class Order(val row: String, val type: Type = Type.ASC) : Attributes {

    /**
     * 排序类型
     */
    enum class Type {

        /**
         * 升序
         */
        ASC,

        /**
         * 降序
         */
        DESC
    }

    /** 语句 */
    override val query: String
        get() {
            return "`${row.asFormattedColumnName()}` $type"
        }
}