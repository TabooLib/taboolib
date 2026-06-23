package taboolib.expansion.container

import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.expansion.CollectionTableInfo
import taboolib.expansion.ContainerOperator
import taboolib.expansion.migration.MigrationConfig
import taboolib.expansion.migration.MigrationFiles
import taboolib.expansion.operator.ContainerOperatorImpl
import taboolib.expansion.orm.AnalyzedClass
import taboolib.expansion.orm.AnalyzedClassMember
import taboolib.expansion.orm.AnalyzedClassMember.Companion.resolveTableName
import taboolib.module.database.ColumnBuilder
import taboolib.module.database.Host
import taboolib.module.database.Table
import java.util.concurrent.ConcurrentHashMap

abstract class Container<T : ColumnBuilder>(val host: Host<T>) {

    val map = ConcurrentHashMap<String, ContainerOperator>()
    val dataSource = host.createDataSource(autoRelease = false)

    /** 表前缀，所有表名统一加此前缀 */
    var tablePrefix: String = ""

    /** 解析带前缀的表名 */
    fun resolveTableName(clazz: Class<*>): String = tablePrefix + clazz.resolveTableName()

    /** class → 表名映射（用于 @LinkTable 关联查询） */
    val classTableMap = ConcurrentHashMap<Class<*>, String>()

    /** class → ContainerOperator 映射（用于 @LinkTable 级联保存） */
    val classOperatorMap = ConcurrentHashMap<Class<*>, ContainerOperator>()

    /** class → 容器关联表信息列表 */
    val collectionTableMap = ConcurrentHashMap<Class<*>, List<CollectionTableInfo>>()

    /** 手动建表 SQL（非 null 时跳过自动建表） */
    internal var manualTableStatements: List<String>? = null

    /** 版本迁移配置 */
    internal var migrationInstance: MigrationConfig? = null

    /** SQL 文件迁移配置 */
    internal var migrationFiles: MigrationFiles? = null

    /** 数据库方言（由子类提供） */
    protected abstract val dialect: DatabaseDialect

    /** 创建表 */
    protected open fun createTableObject(type: AnalyzedClass, name: String): Table<*, *> {
        return dialect.createTable(type, name, host)
    }

    /** 创建容器类型子表 */
    protected open fun createCollectionTableObject(
        parentType: AnalyzedClass,
        parentTableName: String,
        member: AnalyzedClassMember,
        childTableName: String
    ): Table<*, *> {
        return dialect.createCollectionTable(parentType, parentTableName, member, childTableName, host)
    }

    /** 创建表 */
    open fun createTable(type: AnalyzedClass, name: String) {
        // 先注册 @LinkTable 关联类的表（确保关联类的 operator 在主类之前就绑定）
        for (member in type.linkMembers) {
            val linkClass = member.linkTableClass ?: continue
            if (!classOperatorMap.containsKey(linkClass)) {
                val linkType = AnalyzedClass.of(linkClass)
                val linkName = resolveTableName(linkClass)
                createTable(linkType, linkName)
            }
        }
        classTableMap[type.clazz] = name
        // 创建容器类型子表
        val collectionInfos = mutableListOf<CollectionTableInfo>()
        if (type.hasCollectionMembers) {
            val primaryMember = type.primaryMember ?: error(
                "容器类型成员需要主类有 @Id 字段。Collection members require the parent class to have an @Id field."
            )
            for (member in type.collectionMembers) {
                val childTableName = "${name}_${member.name}"
                val fkColumnName = "parent_${primaryMember.name}"
                val childTable = createCollectionTableObject(type, name, member, childTableName)
                collectionInfos += CollectionTableInfo(childTableName, fkColumnName, member, childTable)
            }
        }
        if (collectionInfos.isNotEmpty()) {
            collectionTableMap[type.clazz] = collectionInfos
        }
        val operator = ContainerOperatorImpl(
            createTableObject(type, name), dataSource,
            classTableMap = classTableMap, classOperatorMap = classOperatorMap,
            collectionTableInfos = collectionInfos.ifEmpty { null }
        )
        map[name] = operator
        classOperatorMap[type.clazz] = operator
    }

    /** 初始化所有表 */
    open fun init() {
        val migrationRunner = migrationFiles?.runner(dataSource)
        val freshDatabase = migrationRunner?.isFreshDatabase() == true
        if (!freshDatabase) {
            migrationRunner?.migrate()
        }

        if (manualTableStatements != null) {
            // 手动建表：执行用户提供的 SQL
            dataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    manualTableStatements!!.forEach { stmt.executeUpdate(it) }
                }
            }
        } else {
            // 自动建表
            map.forEach { it.value.table.createTable(dataSource) }
            // 创建容器子表
            collectionTableMap.values.forEach { infos ->
                infos.forEach { it.table.createTable(dataSource) }
            }
        }
        if (freshDatabase) {
            migrationRunner?.baselineLatest()
        }
        // 版本迁移
        migrationInstance?.let { runMigrations(it) }
        // 方言后处理（如创建索引）
        dialect.postInit(this)
    }

    /** 执行版本迁移 */
    private fun runMigrations(config: MigrationConfig) {
        if (config.versions.isEmpty()) return
        dataSource.connection.use { conn ->
            // 创建 meta 表
            conn.createStatement().use { stmt ->
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS _ptc_meta (" +
                    "table_name VARCHAR(255) NOT NULL PRIMARY KEY, " +
                    "version INT NOT NULL DEFAULT 0)"
                )
            }
            // 对每个注册的表执行迁移
            for (tableName in map.keys) {
                // 查询当前版本
                val currentVersion = conn.prepareStatement(
                    "SELECT version FROM _ptc_meta WHERE table_name = ?"
                ).use { ps ->
                    ps.setString(1, tableName)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) rs.getInt("version") else 0
                    }
                }
                // 执行待迁移版本
                val pending = config.versions.filter { it.key > currentVersion }
                if (pending.isEmpty()) continue
                pending.forEach { (_, sqls) ->
                    conn.createStatement().use { stmt ->
                        sqls.forEach { stmt.executeUpdate(it) }
                    }
                }
                // 更新版本号
                val maxVersion = pending.keys.max()
                if (currentVersion == 0) {
                    conn.prepareStatement(
                        "INSERT INTO _ptc_meta (table_name, version) VALUES (?, ?)"
                    ).use { ps ->
                        ps.setString(1, tableName)
                        ps.setInt(2, maxVersion)
                        ps.executeUpdate()
                    }
                } else {
                    conn.prepareStatement(
                        "UPDATE _ptc_meta SET version = ? WHERE table_name = ?"
                    ).use { ps ->
                        ps.setInt(1, maxVersion)
                        ps.setString(2, tableName)
                        ps.executeUpdate()
                    }
                }
            }
        }
    }

    /** 获取路径 */
    open fun path(): String {
        return host.connectionUrl.toString()
    }

    /** 关闭连接 */
    open fun close() {
        dataSource.invokeMethod<Void>("close")
    }

    /** 获取控制器 */
    open fun operator(name: String): ContainerOperator {
        return map[name] ?: error("Table not found")
    }
}
