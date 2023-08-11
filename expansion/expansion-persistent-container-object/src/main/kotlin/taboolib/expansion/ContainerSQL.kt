package taboolib.expansion

import taboolib.module.database.*

class ContainerSQL(
    host: String,
    port: Int,
    user: String,
    password: String,
    database: String,
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
) : Container<SQL>(HostSQL(host, port.toString(), user, password, database).also {
    if (clearFlags) {
        it.flags.clear()
    }
    it.flags.addAll(flags)
    if (ssl != null) {
        it.flags -= "useSSL=false"
        it.flags += "sslMode=$ssl"
    }
}) {

    override fun createTableObject(type: AnalyzedClass, name: String): Table<*, *> {
        return Table(name, host) {
            add { id() }
            type.members.forEach { member ->
                when {
                    // 字符串
                    member.isString || member.isEnum -> add(member.name) {
                        type(ColumnTypeSQL.VARCHAR, member.length) { options(member) }
                    }
                    // UUID
                    member.isUUID -> add(member.name) {
                        type(ColumnTypeSQL.CHAR, 36) { options(member) }
                    }
                    // 其他类型
                    else -> add(member.name) {
                        val customType = CustomTypeFactory.getCustomTypeByClass(member.returnType)
                        if (customType == null) {
                            type(member.type()) { options(member) }
                        } else {
                            type(customType.typeSQL, customType.length) { options(member) }
                        }
                    }
                }
            }
        }
    }

    private fun ColumnSQL.options(member: AnalyzedClassMember) {
        if (member.isKey) {
            options(ColumnOptionSQL.KEY)
        } else if (member.isUniqueKey) {
            options(ColumnOptionSQL.UNIQUE_KEY)
        }
    }

    private fun AnalyzedClassMember.type(): ColumnTypeSQL {
        return when {
            isBoolean -> ColumnTypeSQL.BOOLEAN
            isByte -> ColumnTypeSQL.TINYINT
            isShort -> ColumnTypeSQL.SMALLINT
            isInt -> ColumnTypeSQL.INT
            isLong -> ColumnTypeSQL.BIGINT
            isFloat -> ColumnTypeSQL.FLOAT
            isDouble -> ColumnTypeSQL.DOUBLE
            isChar -> ColumnTypeSQL.INT
            else -> error("Unsupported type: $name (${returnType})")
        }
    }
}