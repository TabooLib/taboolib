package taboolib.module.database

/**
 * SQL 数据列
 *
 * @author sky
 * @since 2018-05-14 19:09
 */
class ColumnSQL(val type: ColumnTypeSQL, val name: String) : Column() {

    /**
     * 类型参数
     * 例如 tinyint(M), int(M), decimal(N, M)
     */
    internal val parameter = arrayOf(0, 0)

    internal var options = arrayOf<ColumnOptionSQL>()

    /**
     * 为该列赋予一项默认值
     * 如果参数含有单引号则需要添加转义符例如 "\\'"
     * 非字符串的特殊类型需在参数前添加 "$" 符号
     */
    internal var def: Any? = null

    internal var onUpdate: String? = null

    internal var indexType = IndexType.DEFAULT

    /**
     * 倒序索引，在 SQL 8.0 中有效，在此之前可以使用，但不会生效。
     */
    internal var desc = false

    /** 设置参数 */
    fun parameter(parameter1: Int = 0, parameter2: Int = 0) {
        parameter[0] = parameter1
        parameter[1] = parameter2
    }

    /** 设置选项 */
    fun options(vararg options: ColumnOptionSQL) {
        this.options += options
    }

    /** 默认值 */
    fun def(def: Any? = null) {
        this.def = def
    }

    /** 当更新时 */
    fun onUpdate(onUpdate: String? = null) {
        this.onUpdate = onUpdate
    }

    /** 索引类型 */
    fun indexType(indexType: IndexType) {
        this.indexType = indexType
    }

    /** 倒序索引 */
    fun indexDesc(desc: Boolean) {
        this.desc = desc
    }

    override val query: String
        get() = Statement(name.asFormattedColumnName()).also { stat ->
            when {
                // 无参数
                parameter[0] == 0 && parameter[1] == 0 && !type.isRequired -> stat.addSegment(type.name)
                // 一个参数
                parameter[1] == 0 -> stat.addSegment("$type(${parameter[0]})")
                // 完整参数
                else -> stat.addSegment("$type(${parameter[0]},${parameter[1]})")
            }
        }.addSegment(queryOptions).build()

    val queryOptions: String
        get() = Statement().also { stat ->
            stat.addSegment(options.filter { it !== ColumnOptionSQL.UNIQUE_KEY && it !== ColumnOptionSQL.KEY }.map { it.query })
            // 默认值
            if (def != null) {
                stat.addSegment("DEFAULT")
                stat.addSpecialValue(def!!)
            }
            // 当更新时
            if (onUpdate != null) {
                stat.addSegment("ON UPDATE").addSegment(onUpdate!!)
            }
        }.build()

    override fun toString(): String {
        return "ColumnSQL(type=$type, name='$name', parameter=${parameter.contentToString()}, options=${options.contentToString()}, def=$def, onUpdate=$onUpdate, indexType=$indexType, desc=$desc, query='$query')"
    }
}