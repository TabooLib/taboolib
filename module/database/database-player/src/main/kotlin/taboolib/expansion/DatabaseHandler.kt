package taboolib.expansion

import taboolib.common.io.newFile
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.pluginId
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.database.HostSQL
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 玩家数据库实例。
 *
 * 该变量用于存储玩家数据库的引用。初始值为 null，表示数据库尚未初始化。
 * 在设置数据库连接后，此变量将被赋予一个 [Database] 实例。
 */
var playerDatabase: Database? = null

/**
 * 玩家数据容器。
 *
 * 该变量用于存储玩家的数据容器。它是一个线程安全的并发哈希映射，
 * 以玩家的 UUID 为键，对应的 [DataContainer] 为值。
 * 这允许快速、安全地访问和修改玩家的数据。
 */
val playerDataContainer = ConcurrentHashMap<UUID, DataContainer>()

/**
 * 玩家自动数据容器。
 *
 * 该变量用于存储玩家的自动数据容器。它是一个线程安全的并发哈希映射，
 * 以玩家的 UUID 为键，对应的 [AutoDataContainer] 为值。
 * 这允许自动同步和管理玩家的数据，提供了一种更高级的数据处理机制。
 */
val playerAutoDataContainer = ConcurrentHashMap<UUID, AutoDataContainer>()

/**
 * 设置玩家数据库
 *
 * @param conf 配置部分
 * @param table 表名，默认从配置中获取
 * @param flags 额外的数据库连接标志
 * @param clearFlags 是否清除现有标志
 * @param ssl SSL 模式
 */
fun setupPlayerDatabase(
    conf: ConfigurationSection,
    table: String = conf.getString("table", "")!!,
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
) {
    val hostSQL = HostSQL(conf)
    if (clearFlags) {
        hostSQL.flags.clear()
    }
    hostSQL.flags.addAll(flags)
    if (ssl != null) {
        hostSQL.flags -= "useSSL=false"
        hostSQL.flags += "sslMode=$ssl"
    }
    playerDatabase = Database(TypeSQL(hostSQL, table))
}

/**
 * 设置玩家数据库
 *
 * @param host 主机地址
 * @param port 端口号
 * @param user 用户名
 * @param password 密码
 * @param database 数据库名
 * @param table 表名
 * @param flags 额外的数据库连接标志
 * @param clearFlags 是否清除现有标志
 * @param ssl SSL 模式
 */
fun setupPlayerDatabase(
    host: String = "localhost",
    port: Int = 3306,
    user: String = "root",
    password: String = "root",
    database: String = "minecraft",
    table: String = "${pluginId.lowercase()}_database",
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
) {
    val conf = Configuration.fromMap(mapOf("host" to host, "port" to port, "user" to user, "password" to password, "database" to database, "table" to table))
    setupPlayerDatabase(conf, table, flags, clearFlags, ssl)
}

/**
 * 构建玩家数据库
 *
 * @param conf 配置部分
 * @param table 表名，默认从配置中获取
 * @param flags 额外的数据库连接标志
 * @param clearFlags 是否清除现有标志
 * @param ssl SSL 模式
 * @return 数据库实例
 */
fun buildPlayerDatabase(
    conf: ConfigurationSection,
    table: String = conf.getString("table", "")!!,
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
): Database {
    val hostSQL = HostSQL(conf)
    if (clearFlags) {
        hostSQL.flags.clear()
    }
    hostSQL.flags.addAll(flags)
    if (ssl != null) {
        hostSQL.flags -= "useSSL=false"
        hostSQL.flags += "sslMode=$ssl"
    }
    return Database(TypeSQL(hostSQL, table))
}

/**
 * 设置玩家 SQLite 数据库
 *
 * @param file 数据库文件
 */
fun setupPlayerDatabase(file: File = newFile(getDataFolder(), "data.db")) {
    playerDatabase = Database(TypeSQLite(file))
}

/**
 * 设置玩家 SQLite 数据库
 *
 * @param file 数据库文件
 * @param tableName 表名
 */
fun setupPlayerDatabase(file: File = newFile(getDataFolder(), "data.db"), tableName: String) {
    playerDatabase = Database(TypeSQLite(file, tableName))
}

/**
 * 构建玩家 SQLite 数据库
 *
 * @param file 数据库文件
 * @param table 表名
 * @return 数据库实例
 */
fun buildPlayerDatabase(file: File = newFile(getDataFolder(), "data.db"), table: String): Database {
    return Database(TypeSQLite(file, table))
}

/**
 * 获取玩家的数据容器
 *
 * @return 数据容器
 * @throws IllegalStateException 如果数据容器不可用
 */
fun ProxyPlayer.getDataContainer(): DataContainer {
    return playerDataContainer[uniqueId] ?: error("unavailable")
}

/**
 * 为玩家设置数据容器
 *
 * @param usernameMode 是否使用用户名模式
 */
fun ProxyPlayer.setupDataContainer(usernameMode: Boolean = false) {
    val user = if (usernameMode) name else uniqueId.toString()
    playerDataContainer[uniqueId] = DataContainer(user, playerDatabase!!)
}

/**
 * 为 UUID 设置玩家数据容器
 */
fun UUID.setupPlayerDataContainer() {
    playerDataContainer[this] = DataContainer(this.toString(), playerDatabase!!)
}

/**
 * 获取 UUID 对应的玩家数据容器
 *
 * @return 数据容器
 * @throws IllegalStateException 如果数据容器不可用
 */
fun UUID.getPlayerDataContainer(): DataContainer {
    return playerDataContainer[this] ?: error("unavailable")
}

/**
 * 释放 UUID 对应的玩家数据容器
 */
fun UUID.releasePlayerDataContainer() {
    playerDataContainer.remove(this)
}

/**
 * 释放玩家的数据容器
 */
fun ProxyPlayer.releaseDataContainer() {
    playerDataContainer.remove(uniqueId)
}

/**
 * 获取 UUID 对应的自动数据容器
 *
 * @return 自动数据容器
 */
fun UUID.getAutoDataContainer(): AutoDataContainer {
    return playerAutoDataContainer.computeIfAbsent(this) {
        AutoDataContainer(this.toString(), playerDatabase!!)
    }
}

/**
 * 释放 UUID 对应的自动数据容器
 */
fun UUID.releaseAutoDataContainer() {
    playerAutoDataContainer.remove(this)
}