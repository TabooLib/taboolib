package taboolib.expansion.container

import taboolib.module.database.HostPostgreSQL
import taboolib.module.database.PostgreSQL
import taboolib.module.database.use

class ContainerPostgreSQL(
    host: String,
    port: Int,
    user: String,
    password: String,
    database: String,
    schema: String = "public",
    flags: List<String> = emptyList(),
) : Container<PostgreSQL>(HostPostgreSQL(host, port.toString(), user, password, database, schema).also {
    it.flags.addAll(flags)
}) {

    override val dialect: DatabaseDialect = PostgreSQLDialect

    override fun init() {
        // PostgreSQL 需要先创建 schema，才能建表
        val schemas = mutableSetOf<String>()
        map.keys.forEach { tableName ->
            val dotIndex = tableName.indexOf('.')
            if (dotIndex > 0) {
                schemas.add(tableName.substring(0, dotIndex))
            }
        }
        if (schemas.isNotEmpty()) {
            dataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    schemas.forEach { schema ->
                        stmt.executeUpdate("CREATE SCHEMA IF NOT EXISTS \"$schema\"")
                    }
                }
            }
        }
        // 调用父类 init（建表 + 迁移 + postInit 创建索引）
        super.init()
    }
}
