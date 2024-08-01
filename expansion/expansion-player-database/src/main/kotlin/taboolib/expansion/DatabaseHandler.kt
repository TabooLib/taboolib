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

var playerDatabase: Database? = null

val playerDataContainer = ConcurrentHashMap<UUID, DataContainer>()

val playerReadOnlyDataContainer = ConcurrentHashMap<UUID, ReadOnlyDataContainer>()

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

fun setupPlayerDatabase(file: File = newFile(getDataFolder(), "data.db")) {
    playerDatabase = Database(TypeSQLite(file))
}

fun setupPlayerDatabase(file: File = newFile(getDataFolder(), "data.db"), tableName: String) {
    playerDatabase = Database(TypeSQLite(file, tableName))
}

fun ProxyPlayer.getDataContainer(): DataContainer {
    return playerDataContainer[uniqueId] ?: error("unavailable")
}

fun ProxyPlayer.setupDataContainer(usernameMode: Boolean = false) {
    val user = if (usernameMode) name else uniqueId.toString()
    playerDataContainer[uniqueId] = DataContainer(user, playerDatabase!!)
}

fun UUID.setupPlayerDataContainer() {
    playerDataContainer[this] = DataContainer(this.toString(), playerDatabase!!)
}

fun UUID.getPlayerDataContainer(): DataContainer {
    return playerDataContainer[this] ?: error("unavailable")
}

fun UUID.releasePlayerDataContainer() {
    playerDataContainer.remove(this)
}

fun ProxyPlayer.releaseDataContainer() {
    playerDataContainer.remove(uniqueId)
}

// 获取只读容器
fun UUID.getReadOnlyDataContainer(): ReadOnlyDataContainer {
    return playerReadOnlyDataContainer.computeIfAbsent(this) {
        ReadOnlyDataContainer(this.toString(), playerDatabase!!)
    }
}

// 释放只读容器
fun UUID.releaseReadOnlyDataContainer() {
    playerReadOnlyDataContainer.remove(this)
}
