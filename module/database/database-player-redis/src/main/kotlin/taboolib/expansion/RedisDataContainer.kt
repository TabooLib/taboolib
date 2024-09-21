package taboolib.expansion

import taboolib.common.platform.function.submitAsync
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Redis 数据容器
 * get 时优先判断Redis缓存 若无则设置数据到缓存
 * set 时删除Redis缓存
 *
 * @property user 用户标识
 * @property database 数据库实例
 * @property redis 数据管理器
 */
class RedisDataContainer(val user: String, val database: Database, val redis: RedisDatabaseHandler) {

    /**
     * 设置指定键的值并立即保存
     *
     * @param key 键
     * @param value 值
     */
    operator fun set(key: String, value: Any) {
        redis.connection?.delete(key)
        database[user, key] = value.toString()
    }

    /**
     * 设置指定键的值，并在指定延迟后删除 (不进入数据库)
     *
     * @param key 键
     * @param value 值
     * @param delay 延迟时间
     * @param timeUnit 时间单位
     */
    fun setDelayed(key: String, value: Any, delay: Long = 3L, timeUnit: TimeUnit = TimeUnit.SECONDS) {
        redis.connection?.setEx(key, value.toString(), delay, timeUnit)
    }

    /**
     * 获取指定键的值 优先从缓存中取出
     *
     * @param key 键
     * @return 对应的值，如果不存在则返回 null
     */
    operator fun get(key: String): String? {
        val redisCache = redis.connection?.get(key)
        if (redisCache != null) {
            return redisCache
        }
        val data = database[user, key]
        if (data != null) {
            redis.connection?.setEx(key, data, REDIS_SECONDS, REDIS_TIMEOUT)
            return data
        }
        return null
    }

    /**
     * 返回对象的字符串表示
     *
     * @return 对象的字符串表示
     */
    override fun toString(): String {
        return "RedisDataContainer(user='$user')"
    }

    companion object {

        /**
         *  缓存设置多长时间后删除
         *
         *  默认为 30分钟
         */
        var REDIS_SECONDS = 1800L

        /**
         *  缓存设置多长时间后删除 - 单位
         */
        var REDIS_TIMEOUT = TimeUnit.SECONDS
    }
}
