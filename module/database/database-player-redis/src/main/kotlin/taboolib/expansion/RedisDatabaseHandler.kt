package taboolib.expansion

import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.pluginId
import taboolib.library.configuration.ConfigurationSection
import java.util.concurrent.ConcurrentHashMap

/**
 *  创建 Redis 数据管理器
 *  配置文件要求如下:
 *  Database:
 *   enable: false
 *   host: localhost
 *   port: 3306
 *   user: root
 *   password: root
 *   database: minecraft
 *   table: elytra
 * Redis:
 *   host: localhost
 *   port: 6379
 *   user: user
 *   password: password
 *   connect: 32
 *   timeout: 1000
 */
class RedisDatabaseHandler(
    val conf: ConfigurationSection,
    var table: String = "",
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
    dataFile: String = "data.db",
) {

    val database: Database
    private var connector: SingleRedisConnector? = null
    var connection: SingleRedisConnection? = null

    /**
     * 玩家Redis数据容器。
     *
     * 该变量用于存储玩家的数据容器。它是一个线程安全的并发哈希映射，
     * 以玩家的 UUID 为键，对应的 [DataContainer] 为值。
     * 这允许快速、安全地访问和修改玩家的数据。
     */
    val redisDataContainer = ConcurrentHashMap<String, RedisDataContainer>()

    init {
        table = conf.getConfigurationSection("Database")!!.getString("table", pluginId)!!
        database = if (conf.getBoolean("enable")) {
            buildPlayerDatabase(conf, table, flags, clearFlags, ssl)
        } else {
            buildPlayerDatabase(newFile(getDataFolder(), dataFile), table)
        }
        val redis = conf.getConfigurationSection("Redis")!!
        if (redis.getBoolean("enable")) {
            connector = AlkaidRedis.create().fromConfig(redis)
            connection?.close()
            connection = connector!!.connect().connection()
        }
    }

    /**
     *  初始化数据容器
     *  这个user作为索引，不一定非得是玩家的UUID
     */
    fun setupRedisDataContainer(user: String): RedisDataContainer {
        return redisDataContainer.computeIfAbsent(user) { RedisDataContainer(user, database, this) }
    }

    /**
     *  获取数据容器
     */
    fun getRedisDataContainer(user: String): RedisDataContainer {
        return redisDataContainer[user] ?: error("unavailable database container ${user}")
    }

    /**
     *  移除数据容器
     */
    fun removeRedisDataContainer(user: String) {
        redisDataContainer.remove(user)
    }

}
