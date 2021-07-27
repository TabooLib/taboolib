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

    fun parameter(parameter1: Int = 0, parameter2: Int = 0) {
        parameter[0] = parameter1
        parameter[1] = parameter2
    }

    fun options(vararg options: ColumnOptionSQL) {
        this.options += options
    }

    fun def(def: Any? = null) {
        this.def = def
    }

    fun onUpdate(onUpdate: String? = null) {
        this.onUpdate = onUpdate
    }

    fun indexType(indexType: IndexType) {
        this.indexType = indexType
    }

    fun indexDesc(desc: Boolean) {
        this.desc = desc
    }

    override val query: String
        get() = when {
            parameter[0] == 0 && parameter[1] == 0 && !type.isRequired -> {
                "`$name` $type $queryOptions"
            }
            parameter[1] == 0 -> {
                "`$name` $type(${parameter[0]}) $queryOptions"
            }
            else -> {
                "`$name` $type(${parameter[0]},${parameter[1]}) $queryOptions"
            }
        }

    val queryOptions: String
        get() {
            var query = options.filter { it !== ColumnOptionSQL.UNIQUE_KEY && it !== ColumnOptionSQL.KEY }.joinToString(" ") { it.query }
            if (def is String) {
                query += if (def.toString().startsWith("$")) {
                    " DEFAULT ${def.toString().substring(1)}"
                } else {
                    " DEFAULT '$def'"
                }
            } else if (def != null) {
                query += " DEFAULT $def"
            }
            if (onUpdate != null) {
                query += " On UPDATE $onUpdate"
            }
            return query
        }

    override fun toString(): String {
        return "ColumnSQL(type=$type, name='$name', parameter=${parameter.contentToString()}, options=${options.contentToString()}, def=$def, onUpdate=$onUpdate, indexType=$indexType, desc=$desc, query='$query')"
    }
}