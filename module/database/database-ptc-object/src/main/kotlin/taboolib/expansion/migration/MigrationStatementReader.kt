package taboolib.expansion.migration

/**
 * 迁移文件语句读取器。
 * 不解析 SQL 语法，只按独占一行的显式标记切分语句，避免误处理字符串、注释或数据库函数体。
 */
object MigrationStatementReader {

    /**
     * 读取迁移文件中的 SQL 语句块。
     * 没有分段标记时，整个文件作为一条语句执行。
     *
     * @param sql SQL 文件正文
     * @param separator 独占一行的语句分段标记
     * @return 待执行 SQL 语句块
     */
    fun read(sql: String, separator: String): List<String> {
        val statements = mutableListOf<String>()
        val builder = StringBuilder()
        for (line in sql.lineSequence()) {
            if (line.trim() == separator) {
                addStatement(statements, builder)
                continue
            }
            builder.appendLine(line)
        }
        addStatement(statements, builder)
        return statements
    }

    /**
     * 写入非空 SQL 语句块。
     *
     * @param statements 已读取语句列表
     * @param builder 当前语句缓冲区
     */
    fun addStatement(statements: MutableList<String>, builder: StringBuilder) {
        val statement = builder.toString().trim()
        if (statement.isNotEmpty()) {
            statements += statement
        }
        builder.clear()
    }
}
