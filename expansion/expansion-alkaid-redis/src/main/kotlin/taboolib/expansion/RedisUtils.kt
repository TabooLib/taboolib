package taboolib.expansion

import taboolib.library.configuration.ConfigurationSection

object RedisUtils {

    private var connectionCache: IRedisConnection? = null

    fun safe(lockKey: String, func: IRedisConnection.() -> Unit) {
        val lock = connection().getLock(lockKey)
        try {
            if (!lock.tryLock()) {
                return
            }
            func.invoke(connection())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            lock.unlock()
        }
    }

    /**
     * 获取 Redis 连接
     * 第一次操作需要传入config 后续可以获取
     */
    fun connection(config: ConfigurationSection? = null): IRedisConnection {
        if (connectionCache != null) {
            return connectionCache as IRedisConnection
        }
        if (config != null) {
            link(config)
        }
        if (connectionCache != null) {
            return connectionCache as IRedisConnection
        }
        throw RuntimeException("Redis 连接失败")
    }

    fun link(config: ConfigurationSection) {
        val type = config.getString("redis.type") ?: "disable"
        val connection = when (type) {
            "single" -> {
                AlkaidRedis.createDefault {
                    it.host = config.getString("redis.single.host") ?: "localhost"
                    it.port = config.getInt("redis.single.port")
                    it.auth = config.getString("redis.single.auth")
                    it.pass = config.getString("redis.single.password")
                    it.connect = config.getInt("redis.single.connect")
                    it.timeout = config.getInt("redis.single.timeout")
                    it.reconnectDelay = config.getLong("redis.single.delay")
                }
            }

            "cluster" -> {
                AlkaidRedis.linkCluster {
                    auth = config.getString("redis.cluster.auth")
                    pass = config.getString("redis.cluster.password")
                    connect = config.getInt("redis.cluster.connect")
                    timeout = config.getInt("redis.cluster.timeout")
                    maxAttempts = config.getInt("redis.cluster.maxAttempts")
                    clientName = config.getString("redis.cluster.clientName") ?: "default"
                    config.getStringList("redis.cluster.nodes").forEach { node ->
                        addNode(node)
                    }
                }.connection()
            }

            else -> {
                null
            }
        }
        connectionCache = connection
    }

}
