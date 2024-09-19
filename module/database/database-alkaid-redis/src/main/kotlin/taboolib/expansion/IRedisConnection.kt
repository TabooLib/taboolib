package taboolib.expansion

import redis.clients.jedis.JedisPubSub
import taboolib.expansion.lock.Lock
import java.util.concurrent.TimeUnit

interface IRedisConnection {

    fun eval(script: String, keys: List<String>, args: List<String>): Any?

    fun eval(script: String, keyC: Int, args: List<String>): Any?

    fun getLock(lockName: String): Lock {
        return Lock(this, lockName)
    }

    fun getLock(lockName: String, action: Lock.() -> Unit): Lock {
        return Lock(this, lockName).apply {
            action.invoke(this)
        }
    }

    /**
     * 关闭连接
     */
    fun close()

    /**
     * 赋值
     *
     * @param key 键
     * @param value 值
     */
    operator fun set(key: String, value: String?)

    fun setNx(key: String, value: String?)

    fun setEx(key: String, value: String?, seconds: Long, timeUnit: TimeUnit) {
        set(key, value)
        expire(key, seconds, timeUnit)
    }

    /**
     * 取值
     *
     * @param key 键
     * @return 值
     */
    operator fun get(key: String): String?

    /**
     * 删除
     *
     * @param key 键
     */
    fun delete(key: String)

    /**
     * 赋值并设置过期时间
     *
     * @param key 键
     * @param value 值
     */
    fun expire(key: String, value: Long, timeUnit: TimeUnit)

    /**
     * 是否存在
     *
     * @param key
     * @return Boolean
     */
    fun contains(key: String): Boolean

    /**
     * 推送信息
     *
     * @param channel 频道
     * @param message 消息
     */
    fun publish(channel: String, message: Any)

    /**
     * 订阅频道
     *
     * @param channel 频道
     * @param patternMode 频道名称是否为正则模式
     * @param func 信息处理函数
     */
    fun subscribe(vararg channel: String, patternMode: Boolean = false, func: RedisMessage.() -> Unit)

    fun createPubSub(patternMode: Boolean, func: RedisMessage.() -> Unit): JedisPubSub

    /**
     * 向集合中添加一个或多个元素
     * @param key 集合名
     * @param value 一个或多个元素
     * @return 返回添加成功的元素数量
     */
    fun sadd(key: String, vararg value: String): Long

    /**
     * 向集合中移除一个或多个元素
     * @param key 集合名
     * @param value 一个或多个元素
     * @return 返回移除成功的元素数量
     */
    fun srem(key: String, vararg value: String): Long

    /**
     * 获取集合中元素的数量
     * @param key 集合名
     * @return 该集合的元素数量
     */
    fun scard(key: String): Long

    /**
     * 获取集合中所有的元素
     * @param key 集合名
     * @return 集合中的所有元素
     */
    fun smembers(key: String): Set<String>

    /**
     * 判断元素是否存在于集合中
     * @param key 集合名
     * @param value 元素名
     * @return 是否存在
     */
    fun sismember(key: String, value: String): Boolean
}
