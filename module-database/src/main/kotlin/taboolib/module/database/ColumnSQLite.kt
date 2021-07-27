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

    fun parameter(parameter1: Int = 0, parameter2: Int = 0) {
        parameter[0] = parameter1
        parameter[1] = parameter2
    }

    fun options(vararg options: ColumnOptionSQLite) {
        this.options += options
    }

    fun def(def: Any? = null) {
        this.def = def
    }

    override val query: String
        get() = when {
            parameter[0] == 0 && parameter[1] == 0 -> {
                "`$name` $type $queryOptions"
            }
            parameter[1] == 0 -> {
                "`$name` $type(${parameter[0]})$queryOptions"
            }
            else -> {
                "`$name` $type(${parameter[0]},${parameter[1]})$queryOptions"
            }
        }

    val queryOptions: String
        get() {
            var query = options.joinToString(" ") { it.query }
            if (def is String) {
                query += if (def.toString().startsWith("$")) {
                    " DEFAULT ${def.toString().substring(1)}"
                } else {
                    " DEFAULT '$def'"
                }
            } else if (def != null) {
                query += " DEFAULT $def"
            }
            return query
        }

    override fun toString(): String {
        return "ColumnSQLite(type=$type, name='$name', parameter=${parameter.contentToString()}, options=${options.contentToString()}, def=$def, query='$query')"
    }
}