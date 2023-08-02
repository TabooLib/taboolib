package taboolib.expansion

import taboolib.module.database.*
import java.io.File

class ContainerSQLite(file: File) : Container<SQLite>(HostSQLite(file)) {

    override fun createTableObject(type: AnalyzedClass, name: String): Table<*, *> {
        return Table(name, host) {
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
                    else -> {
                        CustomObjectType.getDataByClass(member.returnType)?.sqlLiteType ?: error("Unsupported type: ${member.name} (${member.returnType})")
                    }
                }
            }
        }
    }

    private fun ColumnSQLite.options(member: AnalyzedClassMember) {
        if (member.isUniqueKey) {
            options(ColumnOptionSQLite.UNIQUE)
        }
    }
}
