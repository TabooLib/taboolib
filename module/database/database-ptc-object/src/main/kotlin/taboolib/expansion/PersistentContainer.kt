package taboolib.expansion

import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.expansion.container.Container
import taboolib.expansion.container.ContainerPostgreSQL
import taboolib.expansion.container.ContainerSQL
import taboolib.expansion.container.ContainerSQLite
import taboolib.expansion.mapper.DataMapperImpl
import taboolib.expansion.migration.MigrationFiles
import taboolib.expansion.orm.AnalyzedClass
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.ConfigLoader
import taboolib.module.configuration.Configuration
import java.io.File

fun dbFile(file: String = "data.db"): File {
    return newFile(getDataFolder(), file)
}

fun dbSection(file: String = "config.yml", node: String = "database"): ConfigurationSection {
    val conf = ConfigLoader.files[file] ?: return Configuration.empty()
    return conf.configuration.getConfigurationSection(node) ?: Configuration.empty()
}

fun dbSection(section: ConfigurationSection, node: String = "database"): ConfigurationSection {
    return section.getConfigurationSection(node) ?: Configuration.empty()
}

/**
 * 获取数据库配置源
 *
 * @param name 配置文件名
 * @param node 配置节点路径
 * @param file SQLite 数据库文件名（当 enable 为 false 时使用）
 * @param type 数据库类型覆盖（"mysql"、"postgresql"、"sqlite"），为 null 时从配置中读取
 */
fun db(name: String = "config.yml", node: String = "database", file: String = "data.db", type: String? = null): Any {
    val conf = ConfigLoader.files[name] ?: return dbFile(file)
    return if (conf.configuration.getBoolean("$node.enable")) {
        val section = conf.configuration.getConfigurationSection(node) ?: Configuration.empty()
        // 如果指定了 type 参数，将其写入配置节点以便 PersistentContainer 读取
        if (type != null && section is Configuration) {
            section["type"] = type
        }
        section
    } else {
        // 如果 type 明确指定为 sqlite，或未启用数据库，使用文件模式
        newFile(getDataFolder(), file)
    }
}

/**
 * 创建持久化储存容器
 */
fun persistentContainer(
    type: Any = db(),
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
    builder: PersistentContainer.() -> Unit
): PersistentContainer {
    return PersistentContainer(type, flags, clearFlags, ssl, builder)
}

/**
 * 创建持久化储存容器
 */
fun persistentContainer(
    host: String = "localhost",
    port: Int = 3306,
    user: String = "root",
    password: String = "root",
    database: String = "minecraft",
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
    builder: PersistentContainer.() -> Unit,
): PersistentContainer {
    return PersistentContainer(host, port, user, password, database, flags, clearFlags, ssl, builder)
}

class PersistentContainer {

    val container: Container<*>

    /**
     * 设置源
     * - 传入文件类型则为 SQLite 模式
     * - 传入 ConfigurationSection 则根据 type 字段选择数据库类型（默认 mysql）
     *   - type: mysql（默认）→ ContainerSQL
     *   - type: postgresql → ContainerPostgreSQL
     */
    constructor(
        type: Any,
        flags: List<String> = emptyList(),
        clearFlags: Boolean = false,
        ssl: String? = null,
        builder: PersistentContainer.() -> Unit
    ) {
        container = when (type) {
            // SQLite 模式
            is File -> {
                ContainerSQLite(type)
            }
            // SQLite 模式
            is String -> {
                ContainerSQLite(newFile(getDataFolder(), type))
            }
            // SQL 模式（根据 type 字段区分 MySQL / PostgreSQL）
            is ConfigurationSection -> {
                val tablePrefix = type.getString("table-prefix", "")!!
                val dbType = type.getString("type", "mysql")!!.lowercase()
                val c = when (dbType) {
                    "postgresql", "pgsql", "pg" -> {
                        ContainerPostgreSQL(
                            type.getString("host", "localhost")!!,
                            type.getInt("port", 5432),
                            type.getString("user", "postgres")!!,
                            type.getString("password", "")!!,
                            type.getString("database", "minecraft")!!,
                            type.getString("schema", "public")!!,
                            flags,
                        )
                    }
                    else -> {
                        ContainerSQL(
                            type.getString("host", "localhost")!!,
                            type.getInt("port"),
                            type.getString("user", "user")!!,
                            type.getString("password", "user")!!,
                            type.getString("database", "minecraft")!!,
                            flags,
                            clearFlags,
                            ssl
                        )
                    }
                }
                c.tablePrefix = tablePrefix
                c
            }
            // 无效类型
            else -> error("Unsupported source type: $type")
        }
        builder(this)
        container.init()
    }

    /**
     * 设置 SQL 源
     */
    constructor(
        host: String,
        port: Int,
        user: String,
        password: String,
        database: String,
        flags: List<String> = emptyList(),
        clearFlags: Boolean = false,
        ssl: String? = null,
        builder: PersistentContainer.() -> Unit
    ) {
        container = ContainerSQL(host, port, user, password, database, flags, clearFlags, ssl)
        builder(this)
        container.init()
    }

    /**
     * 从数据类创建容器
     */
    inline fun <reified T> new(name: String? = null) = new(T::class.java, name)

    /**
     * 从数据类创建容器
     */
    fun <T> new(type: Class<T>, name: String? = null) {
        container.createTable(AnalyzedClass.of(type), name ?: container.resolveTableName(type))
    }

    /**
     * 启用 SQL 文件迁移。
     * 默认扫描 classpath 下 `ptc-migrations/`，脚本命名为 `V版本__说明.sql`。
     *
     * @param path classpath 中的迁移目录
     * @param statementSeparator 同一个 SQL 文件中分隔多条语句的独占行标记
     * @param baselineOnCreate 新库自动建最新表后，是否把现有迁移脚本标记为已执行
     * @param validateChecksum 是否校验已执行脚本的 SHA-256
     * @param failOnMissingMigration 历史表中存在但资源目录缺失的脚本是否阻止启动
     * @param baselineVersion 已有老库首次接入迁移时，标记为已执行的最大版本
     * @param classLoader 读取迁移脚本资源的类加载器
     */
    fun migrations(
        path: String = MigrationFiles.DEFAULT_PATH,
        statementSeparator: String = MigrationFiles.DEFAULT_STATEMENT_SEPARATOR,
        baselineOnCreate: Boolean = true,
        validateChecksum: Boolean = true,
        failOnMissingMigration: Boolean = true,
        baselineVersion: Int? = null,
        classLoader: ClassLoader = Thread.currentThread().contextClassLoader ?: MigrationFiles::class.java.classLoader,
    ) {
        container.migrationFiles = MigrationFiles(
            path = path,
            statementSeparator = statementSeparator,
            baselineOnCreate = baselineOnCreate,
            validateChecksum = validateChecksum,
            failOnMissingMigration = failOnMissingMigration,
            baselineVersion = baselineVersion,
            classLoader = classLoader,
        )
    }

    /**
     * 解析带前缀的表名
     */
    fun resolveTableName(clazz: Class<*>): String = container.resolveTableName(clazz)

    /**
     * 获取控制器
     */
    operator fun get(name: String): ContainerOperator {
        return container.operator(name)
    }

    /**
     * 获取控制器
     */
    inline fun <reified T> get(): ContainerOperator {
        return get(container.resolveTableName(T::class.java))
    }

    /**
     * 创建 DataMapper（用于已有容器场景）
     *
     * @param cache 可选的 L2 双层缓存
     */
    inline fun <reified T> mapper(cache: L2Cache? = null): DataMapper<T> {
        return DataMapperImpl(T::class.java, this, cache)
    }

    /**
     * 创建 DataMapper，使用自定义 DataCache 实现
     *
     * 传入的 DataCache 同时用于 Bean Cache 和 Query Cache。
     *
     * @param cache 自定义缓存实现（如 Redis、Caffeine 等）
     */
    inline fun <reified T> mapper(cache: DataCache): DataMapper<T> {
        return DataMapperImpl(T::class.java, this, L2Cache(cache, cache))
    }

    /**
     * 关闭容器
     */
    fun close() {
        container.close()
    }

    /**
     * 多表联查
     *
     * ```kotlin
     * val results = container.join {
     *     from<Player>()
     *     innerJoin<PlayerStats> {
     *         on("player.username" eq pre("player_stats.username"))
     *     }
     *     where { "player.active" eq true }
     *     limit(10)
     * }.execute()
     * ```
     */
    fun join(builder: JoinQuery.() -> Unit): JoinQuery {
        return JoinQuery(container.dataSource, tablePrefix = container.tablePrefix).also(builder)
    }

    /**
     * 在事务中执行操作
     *
     * 事务中的所有操作要么全部成功提交，要么全部回滚。
     * 支持跨表操作，所有操作共享同一个数据库连接。
     *
     * ```kotlin
     * val result = container.transaction {
     *     val homes = get<PlayerHome>()
     *     val stats = get<PlayerStats>()
     *
     *     homes.insert(listOf(newHome))
     *     stats.update(playerStats)
     *
     *     if (someError) {
     *         rollback()  // 标记回滚
     *     }
     *
     *     "success"  // 返回值
     * }
     *
     * if (result.isSuccess) {
     *     println("Transaction committed: ${result.getOrNull()}")
     * } else {
     *     println("Transaction failed: ${result.exceptionOrNull()}")
     * }
     * ```
     *
     * @param block 事务代码块
     * @return Result 包含返回值或异常
     */
    fun <R> transaction(block: TransactionContext.() -> R): Result<R> {
        // 事务传递：如果当前线程已有活跃事务，复用外层连接
        val existing = TransactionContext.currentConnection.get()
        if (existing != null) {
            val context = TransactionContext(container, existing)
            return try {
                val result = context.block()
                if (context.shouldRollback()) {
                    Result.failure(TransactionRollbackException("Transaction marked for rollback"))
                } else {
                    Result.success(result)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        // 新事务
        val connection = container.dataSource.connection
        connection.autoCommit = false
        TransactionContext.currentConnection.set(connection)
        return try {
            val context = TransactionContext(container, connection)
            val result = context.block()

            if (context.shouldRollback()) {
                connection.rollback()
                Result.failure(TransactionRollbackException("Transaction marked for rollback"))
            } else {
                connection.commit()
                Result.success(result)
            }
        } catch (e: Exception) {
            try {
                connection.rollback()
            } catch (rollbackEx: Exception) {
                e.addSuppressed(rollbackEx)
            }
            Result.failure(e)
        } finally {
            TransactionContext.currentConnection.remove()
            try {
                connection.autoCommit = true
            } catch (_: Exception) {
            }
            try {
                connection.close()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * 在事务中执行操作（简化版，忽略返回值）
     *
     * @param block 事务代码块
     * @return Result 成功或失败
     */
    fun runTransaction(block: TransactionContext.() -> Unit): Result<Unit> {
        return transaction(block)
    }
}

