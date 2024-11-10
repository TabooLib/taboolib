package taboolib.expansion

import taboolib.common.io.newFile
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.getDataFolder
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.database.HostSQL
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class PlayerDatabase {

    private var database: Database? = null
    private val dataContainer = ConcurrentHashMap<UUID, DataContainer>()

    // 设置基于 SQL 的数据库配置
    fun setupSQLDatabase(
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
        database = Database(TypeSQL(hostSQL, table))
    }

    // 设置基于 SQLite 的数据库配置
    fun setupSQLiteDatabase(file: File = newFile(getDataFolder(), "data.db")) {
        database = Database(TypeSQLite(file))
    }

    // 获取玩家的数据容器
    fun getDataContainer(player: ProxyPlayer): DataContainer {
        return dataContainer[player.uniqueId] ?: error("unavailable")
    }

    // 设置玩家的数据容器
    fun setupDataContainer(player: ProxyPlayer, usernameMode: Boolean = false) {
        val uniqueId = player.uniqueId
        val user = if (usernameMode) player.name else uniqueId.toString()
        dataContainer[uniqueId] = DataContainer(user, database!!)
    }

    // 设置玩家对应的数据容器
    fun setupDataContainer(player: ProxyPlayer) {
        dataContainer[player.uniqueId] = DataContainer(player.uniqueId.toString(), database!!)
    }

    // 设置 UUID 对应的数据容器
    fun setupDataContainer(uuid: UUID) {
        dataContainer[uuid] = DataContainer(uuid.toString(), database!!)
    }

    // 获取 UUID 对应的数据容器
    fun getDataContainer(uuid: UUID): DataContainer {
        return dataContainer[uuid] ?: error("unavailable")
    }

    // 释放 UUID 对应的数据容器
    fun releaseDataContainer(uuid: UUID) {
        dataContainer.remove(uuid)
    }

    // 释放玩家对应的数据容器
    fun releaseDataContainer(player: ProxyPlayer) {
        dataContainer.remove(player.uniqueId)
    }

    fun init(config: ConfigurationSection, table: String, sqlitePath: String, configDatabase: String = "database") {
        try {
            if (config.getBoolean("$configDatabase.enable")) {
                setupSQLDatabase(config.getConfigurationSection(configDatabase)!!, table)
            } else {
                setupSQLiteDatabase(newFile(getDataFolder(), sqlitePath))
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            disablePlugin()
            return
        }
    }

}