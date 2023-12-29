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

    /**
     *  对列进行类型转换 比如字符串类型转小数 应该写 DECIMAL(10,2)
     *  这个参数应该使用DSL模式进行设置
     */
    var castType: String? = null


    /** 语句 */
    override val query: String
        get() {
            if (castType != null) {
                return "CAST(${row.asFormattedColumnName()} AS $castType) $type"
            }
            return "${row.asFormattedColumnName()} $type"
        }
}
