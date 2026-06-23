package taboolib.expansion.container

import taboolib.expansion.CustomTypeFactory
import taboolib.expansion.orm.AnalyzedClass
import taboolib.expansion.orm.AnalyzedClassMember
import taboolib.module.database.*

/**
 * 数据库方言接口（Strategy Pattern）
 * 封装不同数据库的列类型映射和初始化后处理逻辑
 */
interface DatabaseDialect {

    fun createTable(type: AnalyzedClass, name: String, host: Host<*>): Table<*, *>

    fun createCollectionTable(
        parentType: AnalyzedClass,
        parentTableName: String,
        member: AnalyzedClassMember,
        childTableName: String,
        host: Host<*>
    ): Table<*, *>

    /** 建表完成后的后处理（如创建索引） */
    fun postInit(container: Container<*>) {}
}

/**
 * MySQL 方言（从 ContainerSQL 迁移）
 */
object MySQLDialect : DatabaseDialect {

    @Suppress("UNCHECKED_CAST")
    override fun createTable(type: AnalyzedClass, name: String, host: Host<*>): Table<*, *> {
        return Table(name, host as Host<SQL>) {
            // 只有在没有 @Id 字段时才自动添加 id 主键
            if (!type.members.any { it.isPrimary }) {
                add { id() }
            }
            type.members.forEach { member ->
                // 跳过 @Ignore 成员
                if (member.isIgnored) return@forEach
                // 跳过容器类型成员（它们存储在子表中）
                if (member.isCollection) return@forEach
                // @LinkTable 成员：创建外键列而非展开关联类
                if (member.isLinkTable) {
                    val linkedClass = AnalyzedClass.of(member.linkTableClass!!)
                    val linkedPrimary = linkedClass.primaryMember!!
                    val fkColumnName = member.linkTableColumn!!
                    add(fkColumnName) {
                        when {
                            linkedPrimary.hasColumnType -> {
                                val colType = linkedPrimary.columnTypeSQL!!
                                if (colType.isRequired) type(colType, linkedPrimary.length) else type(colType)
                            }
                            linkedPrimary.isIndexedEnum -> type(ColumnTypeSQL.BIGINT)
                            linkedPrimary.isString || linkedPrimary.isEnum -> {
                                if (linkedPrimary.length < 0) type(ColumnTypeSQL.LONGTEXT)
                                else type(ColumnTypeSQL.VARCHAR, linkedPrimary.length)
                            }
                            linkedPrimary.isUUID -> type(ColumnTypeSQL.CHAR, 36)
                            else -> type(linkedPrimary.sqlType())
                        }
                    }
                    return@forEach
                }
                when {
                    // 自定义列类型
                    member.hasColumnType -> add(member.name) {
                        val colType = member.columnTypeSQL!!
                        if (colType.isRequired) type(colType, member.length) { sqlOptions(member) }
                        else type(colType) { sqlOptions(member) }
                    }
                    // IndexedEnum（数值存储）
                    member.isIndexedEnum -> add(member.name) {
                        type(ColumnTypeSQL.BIGINT) { sqlOptions(member) }
                    }
                    // 字符串
                    member.isString || member.isEnum -> add(member.name) {
                        // length == -1 时使用longtext
                        if (member.length < 0) {
                            type(ColumnTypeSQL.LONGTEXT) { sqlOptions(member) }
                        } else {
                            type(ColumnTypeSQL.VARCHAR, member.length) { sqlOptions(member) }
                        }
                    }
                    // UUID
                    member.isUUID -> add(member.name) {
                        type(ColumnTypeSQL.CHAR, 36) { sqlOptions(member) }
                    }
                    // 字节数组
                    member.isByteArray -> add(member.name) {
                        type(ColumnTypeSQL.BLOB) { sqlOptions(member) }
                    }
                    // 其他类型
                    else -> add(member.name) {
                        val customType = if (member.isFlattenedCollection) {
                            CustomTypeFactory.getCustomTypeForCollection(member.returnType, member.collectionElementType!!)
                        } else {
                            CustomTypeFactory.getCustomTypeByClass(member.returnType)
                        }
                        if (customType == null) {
                            type(member.sqlType()) { sqlOptions(member) }
                        } else {
                            type(customType.typeSQL, customType.length) { sqlOptions(member) }
                        }
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun createCollectionTable(
        parentType: AnalyzedClass,
        parentTableName: String,
        member: AnalyzedClassMember,
        childTableName: String,
        host: Host<*>
    ): Table<*, *> {
        val primaryMember = parentType.primaryMember!!
        return Table(childTableName, host as Host<SQL>) {
            add { id() }
            // FK 列：引用主表的 @Id
            add("parent_${primaryMember.name}") {
                when {
                    primaryMember.hasColumnType -> {
                        val colType = primaryMember.columnTypeSQL!!
                        if (colType.isRequired) type(colType, primaryMember.length) else type(colType)
                    }
                    primaryMember.isIndexedEnum -> type(ColumnTypeSQL.BIGINT)
                    primaryMember.isString || primaryMember.isEnum -> {
                        if (primaryMember.length < 0) type(ColumnTypeSQL.LONGTEXT)
                        else type(ColumnTypeSQL.VARCHAR, primaryMember.length)
                    }
                    primaryMember.isUUID -> type(ColumnTypeSQL.CHAR, 36)
                    else -> type(primaryMember.sqlType())
                }
            }
            if (member.isMap) {
                add("map_key") { type(ColumnTypeSQL.VARCHAR, 512) }
                add("map_value") { type(ColumnTypeSQL.VARCHAR, 512) }
            } else {
                add("value") { type(ColumnTypeSQL.VARCHAR, 512) }
                if (member.isList) {
                    add("sort_order") { type(ColumnTypeSQL.INT) }
                }
            }
        }
    }

    private fun ColumnSQL.sqlOptions(member: AnalyzedClassMember) {
        if (member.isKey || member.isPrimary) {
            options(ColumnOptionSQL.KEY)
        }
        if (member.isUniqueKey) {
            options(ColumnOptionSQL.UNIQUE_KEY)
        }
        if (member.isNotNull) {
            options(ColumnOptionSQL.NOTNULL)
        }
    }

    private fun AnalyzedClassMember.sqlType(): ColumnTypeSQL {
        return when {
            isBoolean -> ColumnTypeSQL.BOOLEAN
            isByte -> ColumnTypeSQL.TINYINT
            isShort -> ColumnTypeSQL.SMALLINT
            isInt -> ColumnTypeSQL.INT
            isLong -> ColumnTypeSQL.BIGINT
            isFloat -> ColumnTypeSQL.FLOAT
            isDouble -> ColumnTypeSQL.DOUBLE
            isChar -> ColumnTypeSQL.INT
            isByteArray -> ColumnTypeSQL.BLOB
            else -> error("Unsupported type: $name ($returnType)")
        }
    }
}

/**
 * SQLite 方言（从 ContainerSQLite 迁移）
 */
object SQLiteDialect : DatabaseDialect {

    /** 收集需要建索引的 @Key 字段：table -> columns */
    private val keyColumns = mutableMapOf<String, List<String>>()

    @Suppress("UNCHECKED_CAST")
    override fun createTable(type: AnalyzedClass, name: String, host: Host<*>): Table<*, *> {
        // 记录 @Key 和 @Id 字段用于后续创建索引
        val keys = type.members.filter { it.isKey || it.isPrimary }.map { it.name }
        if (keys.isNotEmpty()) {
            keyColumns[name] = keys
        }
        return Table(name, host as Host<SQLite>) {
            // 没有自定义 @Id 字段时才加自增 id 主键，与 MySQLDialect、Annotations 文档对齐；
            // 有 @Id 时由该字段承担查询键，postInit 建普通索引，避免与自增 id 列同名冲突
            if (!type.members.any { it.isPrimary }) {
                add { id() }
            }
            type.members.forEach { member ->
                // 跳过 @Ignore 成员
                if (member.isIgnored) return@forEach
                // 跳过容器类型成员（它们存储在子表中）
                if (member.isCollection) return@forEach
                // @LinkTable 成员：创建外键列而非展开关联类
                if (member.isLinkTable) {
                    val linkedClass = AnalyzedClass.of(member.linkTableClass!!)
                    val linkedPrimary = linkedClass.primaryMember!!
                    val fkColumnName = member.linkTableColumn!!
                    add(fkColumnName) {
                        when {
                            linkedPrimary.hasColumnType -> type(linkedPrimary.columnTypeSQLite!!, linkedPrimary.length)
                            linkedPrimary.isIndexedEnum -> type(ColumnTypeSQLite.INTEGER)
                            linkedPrimary.isString || linkedPrimary.isEnum -> type(ColumnTypeSQLite.TEXT, linkedPrimary.length)
                            linkedPrimary.isUUID -> type(ColumnTypeSQLite.TEXT, 36)
                            linkedPrimary.canConvertedInteger() -> type(ColumnTypeSQLite.INTEGER)
                            linkedPrimary.canConvertedDecimal() -> type(ColumnTypeSQLite.REAL)
                            else -> type(ColumnTypeSQLite.TEXT)
                        }
                    }
                    return@forEach
                }
                when {
                    // 自定义列类型
                    member.hasColumnType -> add(member.name) {
                        type(member.columnTypeSQLite!!, member.length) { sqliteOptions(member) }
                    }
                    // IndexedEnum（数值存储）
                    member.isIndexedEnum -> add(member.name) {
                        type(ColumnTypeSQLite.INTEGER) { sqliteOptions(member) }
                    }
                    // 字符串
                    member.isString || member.isEnum -> add(member.name) {
                        type(ColumnTypeSQLite.TEXT, member.length) { sqliteOptions(member) }
                    }
                    // UUID
                    member.isUUID -> add(member.name) {
                        type(ColumnTypeSQLite.TEXT, 36) { sqliteOptions(member) }
                    }
                    // 整数
                    member.canConvertedInteger() -> add(member.name) {
                        type(ColumnTypeSQLite.INTEGER) { sqliteOptions(member) }
                    }
                    // 小数
                    member.canConvertedDecimal() -> add(member.name) {
                        type(ColumnTypeSQLite.REAL) { sqliteOptions(member) }
                    }
                    // 字节数组
                    member.isByteArray -> add(member.name) {
                        type(ColumnTypeSQLite.BLOB) { sqliteOptions(member) }
                    }

                    else -> {
                        val customType = if (member.isFlattenedCollection) {
                            CustomTypeFactory.getCustomTypeForCollection(member.returnType, member.collectionElementType!!)
                        } else {
                            CustomTypeFactory.getCustomTypeByClass(member.returnType)
                        } ?: error("Unsupported type: ${member.name} (${member.returnType})")
                        add(member.name) { type(customType.typeSQLite, customType.length) { sqliteOptions(member) } }
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun createCollectionTable(
        parentType: AnalyzedClass,
        parentTableName: String,
        member: AnalyzedClassMember,
        childTableName: String,
        host: Host<*>
    ): Table<*, *> {
        val primaryMember = parentType.primaryMember!!
        return Table(childTableName, host as Host<SQLite>) {
            add { id() }
            // FK 列：引用主表的 @Id
            add("parent_${primaryMember.name}") {
                when {
                    primaryMember.isString || primaryMember.isEnum -> type(ColumnTypeSQLite.TEXT, primaryMember.length)
                    primaryMember.isUUID -> type(ColumnTypeSQLite.TEXT, 36)
                    primaryMember.canConvertedInteger() -> type(ColumnTypeSQLite.INTEGER)
                    primaryMember.canConvertedDecimal() -> type(ColumnTypeSQLite.REAL)
                    else -> type(ColumnTypeSQLite.TEXT)
                }
            }
            if (member.isMap) {
                // Map<K, V> → map_key, map_value
                add("map_key") { type(ColumnTypeSQLite.TEXT, 512) }
                add("map_value") { type(ColumnTypeSQLite.TEXT, 512) }
            } else {
                // List<T> / Set<T> → value
                add("value") { type(ColumnTypeSQLite.TEXT, 512) }
                if (member.isList) {
                    // List 需要 sort_order 保序
                    add("sort_order") { type(ColumnTypeSQLite.INTEGER) }
                }
            }
        }
    }

    override fun postInit(container: Container<*>) {
        // 为 @Key 字段创建 SQLite 索引
        if (keyColumns.isEmpty()) return
        container.dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                keyColumns.forEach { (tableName, columns) ->
                    columns.forEach { col ->
                        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS `idx_${tableName}_${col}` ON `$tableName` (`$col`)")
                    }
                }
            }
        }
        keyColumns.clear()
    }

    private fun ColumnSQLite.sqliteOptions(member: AnalyzedClassMember) {
        // @Id 不加 PRIMARY KEY, 通过 CREATE INDEX 建普通索引（与 MySQL 行为对齐）
        if (member.isUniqueKey) {
            options(ColumnOptionSQLite.UNIQUE)
        }
        if (member.isNotNull) {
            options(ColumnOptionSQLite.NOTNULL)
        }
    }
}

/**
 * PostgreSQL 方言（从 ContainerPostgreSQL 迁移）
 */
object PostgreSQLDialect : DatabaseDialect {

    /** 收集需要建索引的 @Key 字段：table -> columns */
    private val keyColumns = mutableMapOf<String, List<String>>()

    @Suppress("UNCHECKED_CAST")
    override fun createTable(type: AnalyzedClass, name: String, host: Host<*>): Table<*, *> {
        // 记录 @Key 字段用于后续创建索引
        val keys = type.members.filter { it.isKey }.map { it.name }
        if (keys.isNotEmpty()) {
            keyColumns[name] = keys
        }
        return Table(name, host as Host<PostgreSQL>) {
            // 只有在没有 @Id 字段时才自动添加 id 主键
            if (!type.members.any { it.isPrimary }) {
                add { id() }
            }
            type.members.forEach { member ->
                // 跳过 @Ignore 成员
                if (member.isIgnored) return@forEach
                // 跳过容器类型成员（它们存储在子表中）
                if (member.isCollection) return@forEach
                // @LinkTable 成员：创建外键列而非展开关联类
                if (member.isLinkTable) {
                    val linkedClass = AnalyzedClass.of(member.linkTableClass!!)
                    val linkedPrimary = linkedClass.primaryMember!!
                    val fkColumnName = member.linkTableColumn!!
                    add(fkColumnName) {
                        when {
                            linkedPrimary.hasColumnType && linkedPrimary.columnTypePostgreSQL != ColumnTypePostgreSQL._DEFAULT -> {
                                val colType = linkedPrimary.columnTypePostgreSQL!!
                                if (colType.isRequired) type(colType, linkedPrimary.length)
                                else type(colType)
                            }
                            linkedPrimary.hasColumnType && linkedPrimary.columnTypePostgreSQL == ColumnTypePostgreSQL._DEFAULT -> {
                                val pgType = linkedPrimary.columnTypeSQL!!.toPostgreSQL()
                                if (pgType.isRequired) type(pgType, linkedPrimary.length)
                                else type(pgType)
                            }
                            linkedPrimary.isIndexedEnum -> type(ColumnTypePostgreSQL.BIGINT)
                            linkedPrimary.isString || linkedPrimary.isEnum -> {
                                if (linkedPrimary.length < 0) type(ColumnTypePostgreSQL.TEXT)
                                else type(ColumnTypePostgreSQL.VARCHAR, linkedPrimary.length)
                            }
                            linkedPrimary.isUUID -> type(ColumnTypePostgreSQL.UUID)
                            else -> type(linkedPrimary.pgType())
                        }
                    }
                    return@forEach
                }
                when {
                    // 自定义列类型（仅当显式指定了 postgresql 类型时）
                    member.hasColumnType && member.columnTypePostgreSQL != ColumnTypePostgreSQL._DEFAULT -> add(member.name) {
                        val colType = member.columnTypePostgreSQL!!
                        if (colType.isRequired) type(colType, member.length) { pgOptions(member) }
                        else type(colType) { pgOptions(member) }
                    }
                    // @ColumnType 指定了 SQL 类型但未指定 PostgreSQL 类型 -> 从 SQL 类型推断
                    member.hasColumnType && member.columnTypePostgreSQL == ColumnTypePostgreSQL._DEFAULT -> add(member.name) {
                        val pgType = member.columnTypeSQL!!.toPostgreSQL()
                        if (pgType.isRequired) type(pgType, member.length) { pgOptions(member) }
                        else type(pgType) { pgOptions(member) }
                    }
                    // IndexedEnum（数值存储）
                    member.isIndexedEnum -> add(member.name) {
                        type(ColumnTypePostgreSQL.BIGINT) { pgOptions(member) }
                    }
                    // 字符串
                    member.isString || member.isEnum -> add(member.name) {
                        if (member.length < 0) {
                            type(ColumnTypePostgreSQL.TEXT) { pgOptions(member) }
                        } else {
                            type(ColumnTypePostgreSQL.VARCHAR, member.length) { pgOptions(member) }
                        }
                    }
                    // UUID（原生支持）
                    member.isUUID -> add(member.name) {
                        type(ColumnTypePostgreSQL.UUID) { pgOptions(member) }
                    }
                    // 字节数组
                    member.isByteArray -> add(member.name) {
                        type(ColumnTypePostgreSQL.BYTEA) { pgOptions(member) }
                    }
                    // 其他类型
                    else -> add(member.name) {
                        val customType = if (member.isFlattenedCollection) {
                            CustomTypeFactory.getCustomTypeForCollection(member.returnType, member.collectionElementType!!)
                        } else {
                            CustomTypeFactory.getCustomTypeByClass(member.returnType)
                        }
                        if (customType == null) {
                            type(member.pgType()) { pgOptions(member) }
                        } else {
                            if (customType.typePostgreSQL.isRequired) {
                                type(customType.typePostgreSQL, customType.lengthPostgreSQL) { pgOptions(member) }
                            } else {
                                type(customType.typePostgreSQL) { pgOptions(member) }
                            }
                        }
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun createCollectionTable(
        parentType: AnalyzedClass,
        parentTableName: String,
        member: AnalyzedClassMember,
        childTableName: String,
        host: Host<*>
    ): Table<*, *> {
        val primaryMember = parentType.primaryMember!!
        return Table(childTableName, host as Host<PostgreSQL>) {
            add { id() }
            // FK 列：引用主表的 @Id
            add("parent_${primaryMember.name}") {
                when {
                    primaryMember.hasColumnType && primaryMember.columnTypePostgreSQL != ColumnTypePostgreSQL._DEFAULT -> {
                        val colType = primaryMember.columnTypePostgreSQL!!
                        if (colType.isRequired) type(colType, primaryMember.length) else type(colType)
                    }
                    primaryMember.hasColumnType && primaryMember.columnTypePostgreSQL == ColumnTypePostgreSQL._DEFAULT -> {
                        val pgType = primaryMember.columnTypeSQL!!.toPostgreSQL()
                        if (pgType.isRequired) type(pgType, primaryMember.length) else type(pgType)
                    }
                    primaryMember.isIndexedEnum -> type(ColumnTypePostgreSQL.BIGINT)
                    primaryMember.isString || primaryMember.isEnum -> {
                        if (primaryMember.length < 0) type(ColumnTypePostgreSQL.TEXT)
                        else type(ColumnTypePostgreSQL.VARCHAR, primaryMember.length)
                    }
                    primaryMember.isUUID -> type(ColumnTypePostgreSQL.UUID)
                    else -> type(primaryMember.pgType())
                }
            }
            if (member.isMap) {
                add("map_key") { type(ColumnTypePostgreSQL.VARCHAR, 512) }
                add("map_value") { type(ColumnTypePostgreSQL.VARCHAR, 512) }
            } else {
                add("value") { type(ColumnTypePostgreSQL.VARCHAR, 512) }
                if (member.isList) {
                    add("sort_order") { type(ColumnTypePostgreSQL.INTEGER) }
                }
            }
        }
    }

    override fun postInit(container: Container<*>) {
        // 为 @Key 字段创建 PostgreSQL 索引
        if (keyColumns.isEmpty()) return
        container.dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                keyColumns.forEach { (tableName, columns) ->
                    // 索引名中的点号替换为下划线，避免被当作 schema 限定符
                    val safeIndexName = tableName.replace('.', '_')
                    // 表名含 schema 时需要分别引用："schema"."table"
                    val quotedTableName = tableName.split('.').joinToString(".") { "\"$it\"" }
                    columns.forEach { col ->
                        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS \"idx_${safeIndexName}_${col}\" ON $quotedTableName (\"$col\")")
                    }
                }
            }
        }
        keyColumns.clear()
    }

    private fun ColumnPostgreSQL.pgOptions(member: AnalyzedClassMember) {
        if (member.isKey || member.isPrimary) {
            options(ColumnOptionPostgreSQL.KEY)
        }
        if (member.isUniqueKey) {
            options(ColumnOptionPostgreSQL.UNIQUE)
        }
        if (member.isNotNull) {
            options(ColumnOptionPostgreSQL.NOTNULL)
        }
    }

    private fun AnalyzedClassMember.pgType(): ColumnTypePostgreSQL {
        return when {
            isBoolean -> ColumnTypePostgreSQL.BOOLEAN
            isByte -> ColumnTypePostgreSQL.SMALLINT
            isShort -> ColumnTypePostgreSQL.SMALLINT
            isInt -> ColumnTypePostgreSQL.INTEGER
            isLong -> ColumnTypePostgreSQL.BIGINT
            isFloat -> ColumnTypePostgreSQL.REAL
            isDouble -> ColumnTypePostgreSQL.DOUBLE_PRECISION
            isChar -> ColumnTypePostgreSQL.INTEGER
            isByteArray -> ColumnTypePostgreSQL.BYTEA
            else -> error("Unsupported type: $name ($returnType)")
        }
    }

    /** 将 MySQL 列类型映射为 PostgreSQL 等价类型 */
    private fun ColumnTypeSQL.toPostgreSQL(): ColumnTypePostgreSQL {
        return when (this) {
            ColumnTypeSQL.TINYINT -> ColumnTypePostgreSQL.SMALLINT
            ColumnTypeSQL.SMALLINT -> ColumnTypePostgreSQL.SMALLINT
            ColumnTypeSQL.MEDIUMINT -> ColumnTypePostgreSQL.INTEGER
            ColumnTypeSQL.INT -> ColumnTypePostgreSQL.INTEGER
            ColumnTypeSQL.BIGINT -> ColumnTypePostgreSQL.BIGINT
            ColumnTypeSQL.FLOAT -> ColumnTypePostgreSQL.REAL
            ColumnTypeSQL.DOUBLE -> ColumnTypePostgreSQL.DOUBLE_PRECISION
            ColumnTypeSQL.DECIMAL, ColumnTypeSQL.FIXED -> ColumnTypePostgreSQL.NUMERIC
            ColumnTypeSQL.BIT -> ColumnTypePostgreSQL.INTEGER
            ColumnTypeSQL.SERIAL -> ColumnTypePostgreSQL.BIGSERIAL
            ColumnTypeSQL.BOOL, ColumnTypeSQL.BOOLEAN -> ColumnTypePostgreSQL.BOOLEAN
            ColumnTypeSQL.CHAR -> ColumnTypePostgreSQL.CHAR
            ColumnTypeSQL.VARCHAR -> ColumnTypePostgreSQL.VARCHAR
            ColumnTypeSQL.TINYTEXT, ColumnTypeSQL.TEXT, ColumnTypeSQL.MEDIUMTEXT, ColumnTypeSQL.LONGTEXT -> ColumnTypePostgreSQL.TEXT
            ColumnTypeSQL.TINYBLOB, ColumnTypeSQL.BLOB, ColumnTypeSQL.MEDIUMBLOB, ColumnTypeSQL.LONGBLOB -> ColumnTypePostgreSQL.BYTEA
            ColumnTypeSQL.BINARY, ColumnTypeSQL.VARBINARY -> ColumnTypePostgreSQL.BYTEA
            ColumnTypeSQL.JSON -> ColumnTypePostgreSQL.JSON
            ColumnTypeSQL.DATE -> ColumnTypePostgreSQL.DATE
            ColumnTypeSQL.DATETIME, ColumnTypeSQL.TIMESTAMP -> ColumnTypePostgreSQL.TIMESTAMP
            ColumnTypeSQL.TIME -> ColumnTypePostgreSQL.TIME
            ColumnTypeSQL.YEAR -> ColumnTypePostgreSQL.SMALLINT
            else -> ColumnTypePostgreSQL.TEXT
        }
    }
}
