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
                        val customType = CustomTypeFactory.getCustomTypeByClass(member.returnType) ?: error("Unsupported type: ${member.name} (${member.returnType})")
                        add(member.name) { type(customType.typeSQLite, customType.length) { options(member) } }
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