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
    val parameter = arrayOf(0, 0)

    var options = arrayOf<ColumnOptionSQL>()

    /**
     * 为该列赋予一项默认值
     * 如果参数含有单引号则需要添加转义符例如 "\\'"
     * 非字符串的特殊类型需在参数前添加 "$" 符号
     */
    var def: Any? = null

    var onUpdate: String? = null

    var indexType = IndexType.DEFAULT

    /**
     * 倒序索引，在 SQL 8.0 中有效，在此之前可以使用，但不会生效。
     */
    var desc = false

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

    companion object {

        /**
         * ID 常量（id bigint unsigned not null auto_increment primary key）
         */
        val PRIMARY_KEY_ID = ColumnTypeSQL.BIGINT("id") {
            options = arrayOf(ColumnOptionSQL.UNSIGNED, ColumnOptionSQL.NOTNULL, ColumnOptionSQL.AUTO_INCREMENT, ColumnOptionSQL.PRIMARY_KEY)
        }

        /**
         * GMT_CREATE 常量（gmt_create datetime not null default CURRENT_TIMESTAMP）
         */
        val GMT_CREATE = ColumnTypeSQL.DATETIME("gmt_create") {
            options = arrayOf(ColumnOptionSQL.NOTNULL)
            def = "\$CURRENT_TIMESTAMP"
        }

        /**
         * GMT_MODIFIED 常量（gmt_modified datetime not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP）
         */
        val GMT_MODIFIED = ColumnTypeSQL.DATETIME("gmt_modified") {
            options = arrayOf(ColumnOptionSQL.NOTNULL)
            def = "\$CURRENT_TIMESTAMP"
            onUpdate = "CURRENT_TIMESTAMP"
        }
    }
}