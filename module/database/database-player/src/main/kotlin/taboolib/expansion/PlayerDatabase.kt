package taboolib.expansion

import taboolib.common.io.newFile
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.getDataFolder
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.database.HostSQL
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class PlayerDatabase {

    private var database: Database? = null
    private val dataContainer = ConcurrentHashMap<UUID, DataContainer>()

    /**
     * 设置基于 SQL 的数据库配置
     *
     * @param conf 数据库配置
     * @param table 数据库表名
     * @param flags 数据库连接参数
     * @param clearFlags 是否清除数据库连接参数，默认为 false
     * @param ssl SSL 模式，默认为 null
     */
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

    /**
     * 设置基于 SQLite 的数据库配置
     *
     * @param file 数据库文件
     */
    fun setupSQLiteDatabase(file: File = newFile(getDataFolder(), "data.db")) {
        database = Database(TypeSQLite(file))
    }

    /**
     * 获取玩家的数据容器
     *
     * @param player ProxyPlayer
     * @return 数据容器
     */
    fun getDataContainer(player: ProxyPlayer): DataContainer {
        return dataContainer[player.uniqueId] ?: error("unavailable")
    }

    /**
     * 设置玩家的数据容器
     *
     * @param player ProxyPlayer
     * @param usernameMode 是否使用玩家名作为用户名，默认为 false
     */
    fun setupDataContainer(player: ProxyPlayer, usernameMode: Boolean = false) {
        val uniqueId = player.uniqueId
        val user = if (usernameMode) player.name else uniqueId.toString()
        dataContainer[uniqueId] = DataContainer(user, database!!)
    }

    /**
     * 设置玩家对应的数据容器
     *
     * @param player ProxyPlayer
     */
    fun setupDataContainer(player: ProxyPlayer) {
        dataContainer[player.uniqueId] = DataContainer(player.uniqueId.toString(), database!!)
    }

    /**
     * 设置 UUID 对应的数据容器
     *
     * @param uuid UUID
     */
    fun setupDataContainer(uuid: UUID) {
        dataContainer[uuid] = DataContainer(uuid.toString(), database!!)
    }

    /**
     * 获取 UUID 对应的数据容器
     *
     * @param uuid UUID
     * @return 数据容器
     */
    fun getDataContainer(uuid: UUID): DataContainer {
        return dataContainer[uuid] ?: error("unavailable")
    }

    /**
     * 释放 UUID 对应的数据容器
     *
     * @param uuid UUID
     */
    fun releaseDataContainer(uuid: UUID) {
        dataContainer.remove(uuid)
    }

    /**
     * 释放玩家对应的数据容器
     *
     * @param player ProxyPlayer
     */
    fun releaseDataContainer(player: ProxyPlayer) {
        dataContainer.remove(player.uniqueId)
    }

    /**
     * 初始化数据库配置
     *
     * @param config 配置文件
     * @param table 数据库表名
     * @param sqlitePath SQLite 数据库文件路径
     * @param configDatabase 数据库配置文件名，默认为 "database"
     *
     * @throws Throwable 如果初始化失败，则会抛出异常
     */
    fun init(config: ConfigurationSection, table: String, sqlitePath: String, configDatabase: String = "database") {
        try {
            if (config.getBoolean("$configDatabase.enable")) {
                setupSQLDatabase(config.getConfigurationSection(configDatabase)!!, table)
            } else {
                setupSQLiteDatabase(newFile(getDataFolder(), sqlitePath))
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            return
        }
    }

}