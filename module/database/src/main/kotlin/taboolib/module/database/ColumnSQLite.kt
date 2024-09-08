package taboolib.module.database

/**
 * SQLite 数据列
 *
 * @author sky
 * @since 2018-05-14 19:09
 */
class ColumnSQLite(val type: ColumnTypeSQLite, val name: String) : Column() {

    /**
     * 类型参数
     * 例如 tinyint(M), int(M)
     */
    internal val parameter = arrayOf(0, 0)

    internal var options = arrayOf<ColumnOptionSQLite>()

    /**
     * 为该列赋予一项默认值
     * 如果参数含有单引号则需要添加转义符例如 "\\'"
     * 非字符串的特殊类型需在参数前添加 "$" 符号
     */
    internal var def: Any? = null

    /** 设置参数 */
    fun parameter(parameter1: Int = 0, parameter2: Int = 0) {
        parameter[0] = parameter1
        parameter[1] = parameter2
    }

    /** 设置选项 */
    fun options(vararg options: ColumnOptionSQLite) {
        this.options += options
    }

    /** 默认值 */
    fun def(def: Any? = null) {
        this.def = def
    }

    override val query: String
        get() = Statement(name.asFormattedColumnName()).also { stat ->
            when {
                // 无参数
                parameter[0] == 0 && parameter[1] == 0 -> stat.addSegment(type.name)
                // 一个参数
                parameter[1] == 0 -> stat.addSegment("$type(${parameter[0]})")
                // 完整参数
                else -> stat.addSegment("$type(${parameter[0]},${parameter[1]})")
            }
        }.addSegment(queryOptions).build()

    val queryOptions: String
        get() = Statement().also { stat ->
            stat.addSegment(options.map { it.query })
            // 默认值
            if (def != null) {
                stat.addSegment("DEFAULT")
                stat.addSpecialValue(def!!)
            }
        }.build()

    override fun toString(): String {
        return "ColumnSQLite(type=$type, name='$name', parameter=${parameter.contentToString()}, options=${options.contentToString()}, def=$def, query='$query')"
    }
}