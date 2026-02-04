package taboolib.expansion

import taboolib.module.database.*
import java.io.File

class ContainerSQLite(file: File) : Container<SQLite>(HostSQLite(file)) {

    /** 表名 -> @Key 字段名列表 */
    private val keyColumns = mutableMapOf<String, List<String>>()

    override fun createTableObject(type: AnalyzedClass, name: String): Table<*, *> {
        // 记录 @Key 字段用于后续创建索引
        val keys = type.members.filter { it.isKey }.map { it.name }
        if (keys.isNotEmpty()) {
            keyColumns[name] = keys
        }
        return Table(name, host) {
            // 只有在没有 @Id 字段时才自动添加 id 主键
            if (!type.members.any { it.isPrimary }) {
                add { id() }
            }
            type.members.forEach { member ->
                when {
                    // 字符串
                    member.isString || member.isEnum -> add(member.name) {
                        type(ColumnTypeSQLite.TEXT, member.length) { options(member) }
                    }
                    // UUID
                    member.isUUID -> add(member.name) {
                        type(ColumnTypeSQLite.TEXT, 36) { options(member) }
                    }
                    // 整数
                    member.canConvertedInteger() -> add(member.name) {
                        type(ColumnTypeSQLite.INTEGER) { options(member) }
                    }
                    // 小数
                    member.canConvertedDecimal() -> add(member.name) {
                        type(ColumnTypeSQLite.REAL) { options(member) }
                    }
                    // 字节数组
                    member.isByteArray -> add(member.name) {
                        type(ColumnTypeSQLite.BLOB) { options(member) }
                    }

                    else -> {
                        val customType = CustomTypeFactory.getCustomTypeByClass(member.returnType) ?: error("Unsupported type: ${member.name} (${member.returnType})")
                        add(member.name) { type(customType.typeSQLite, customType.length) { options(member) } }
                    }
                }
            }
        }
    }

    override fun init() {
        super.init()
        // 为 @Key 字段创建 SQLite 索引
        if (keyColumns.isNotEmpty()) {
            dataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    keyColumns.forEach { (tableName, columns) ->
                        columns.forEach { col ->
                            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS `idx_${tableName}_${col}` ON `$tableName` (`$col`)")
                        }
                    }
                }
            }
        }
    }

    private fun ColumnSQLite.options(member: AnalyzedClassMember) {
        if (member.isPrimary) {
            options(ColumnOptionSQLite.PRIMARY_KEY)
        }
        if (member.isUniqueKey) {
            options(ColumnOptionSQLite.UNIQUE)
        }
        if (member.isNotNull) {
            options(ColumnOptionSQLite.NOTNULL)
        }
    }
}
