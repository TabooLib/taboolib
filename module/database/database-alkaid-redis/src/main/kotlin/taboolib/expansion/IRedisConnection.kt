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

    /**
     * 将 key 中储存的数字值增一
     * 如果 key 不存在，那么 key 的值会先被初始化为 0，然后再执行 INCR 操作
     * @param key 键
     * @return 执行命令之后 key 的值
     */
    fun incr(key: String): Long

    /**
     * 将 key 所储存的值加上给定的增量值
     * 如果 key 不存在，那么 key 的值会先被初始化为 0，然后再执行 INCRBY 操作
     * @param key 键
     * @param value 增量值
     * @return 执行命令之后 key 的值
     */
    fun incrBy(key: String, value: Long): Long

    /**
     * 将 key 所储存的值加上浮点数增量
     * 如果 key 不存在，那么会先将 key 的值设为 0，再执行加法操作
     * @param key 键
     * @param value 增量（可以为负数，表示减法）
     * @return 执行命令之后 key 的值
     */
    fun incrByFloat(key: String, value: Double): Double

    /**
     * 将 key 中储存的数字值减一
     * 如果 key 不存在，那么 key 的值会先被初始化为 0，然后再执行 DECR 操作
     * @param key 键
     * @return 执行命令之后 key 的值
     */
    fun decr(key: String): Long

    /**
     * 将 key 所储存的值减去给定的减量值
     * 如果 key 不存在，那么 key 的值会先被初始化为 0，然后再执行 DECRBY 操作
     * @param key 键
     * @param value 减量值
     * @return 执行命令之后 key 的值
     */
    fun decrBy(key: String, value: Long): Long

    /**
     * 将哈希表 key 中的字段 field 的值设为 value
     * @param key 键
     * @param field 字段
     * @param value 值
     * @return 如果字段是新建的返回 1，如果字段已存在并更新返回 0
     */
    fun hset(key: String, field: String, value: String): Long

    /**
     * 获取存储在哈希表中指定字段的值
     * @param key 键
     * @param field 字段
     * @return 字段的值，如果字段不存在返回 null
     */
    fun hget(key: String, field: String): String?

    /**
     * 删除哈希表 key 中的一个或多个指定字段
     * @param key 键
     * @param field 一个或多个字段
     * @return 被成功删除的字段数量
     */
    fun hdel(key: String, vararg field: String): Long

    /**
     * 获取哈希表中所有字段和值
     * @param key 键
     * @return 字段和值的映射
     */
    fun hgetAll(key: String): Map<String, String>

    /**
     * 查看哈希表 key 中指定的字段是否存在
     * @param key 键
     * @param field 字段
     * @return 是否存在
     */
    fun hexists(key: String, field: String): Boolean

    /**
     * 获取哈希表中的所有字段名
     * @param key 键
     * @return 所有字段名
     */
    fun hkeys(key: String): Set<String>

    /**
     * 获取哈希表中的所有值
     * @param key 键
     * @return 所有值
     */
    fun hvals(key: String): List<String>

    /**
     * 获取哈希表中字段的数量
     * @param key 键
     * @return 字段数量
     */
    fun hlen(key: String): Long

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量
     * @param key 键
     * @param field 字段
     * @param value 增量
     * @return 执行命令之后字段的值
     */
    fun hincrBy(key: String, field: String, value: Long): Long

    /**
     * 为哈希表 key 中的指定字段的浮点数值加上增量
     * @param key 键
     * @param field 字段
     * @param value 增量
     * @return 执行命令之后字段的值
     */
    fun hincrByFloat(key: String, field: String, value: Double): Double

    /**
     * 同时将多个 field-value 对设置到哈希表 key 中
     * @param key 键
     * @param hash 字段和值的映射
     */
    fun hmset(key: String, hash: Map<String, String>)

    /**
     * 获取所有给定字段的值
     * @param key 键
     * @param fields 字段列表
     * @return 值列表（不存在的字段返回 null）
     */
    fun hmget(key: String, vararg fields: String): List<String?>

    /**
     * 将一个或多个值插入到列表头部
     * @param key 键
     * @param values 一个或多个值
     * @return 列表的长度
     */
    fun lpush(key: String, vararg values: String): Long

    /**
     * 将一个或多个值插入到列表尾部
     * @param key 键
     * @param values 一个或多个值
     * @return 列表的长度
     */
    fun rpush(key: String, vararg values: String): Long

    /**
     * 移除并返回列表的第一个元素
     * @param key 键
     * @return 列表的第一个元素，如果列表为空返回 null
     */
    fun lpop(key: String): String?

    /**
     * 移除并返回列表的最后一个元素
     * @param key 键
     * @return 列表的最后一个元素，如果列表为空返回 null
     */
    fun rpop(key: String): String?

    /**
     * 获取列表指定范围内的元素
     * @param key 键
     * @param start 开始索引
     * @param stop 结束索引
     * @return 指定范围内的元素列表
     */
    fun lrange(key: String, start: Long, stop: Long): List<String>

    /**
     * 获取列表长度
     * @param key 键
     * @return 列表的长度
     */
    fun llen(key: String): Long

    /**
     * 通过索引获取列表中的元素
     * @param key 键
     * @param index 索引
     * @return 列表中指定索引的元素，如果索引超出范围返回 null
     */
    fun lindex(key: String, index: Long): String?

    /**
     * 通过索引设置列表元素的值
     * @param key 键
     * @param index 索引
     * @param value 值
     */
    fun lset(key: String, index: Long, value: String)

    /**
     * 以秒为单位返回 key 的剩余过期时间
     * @param key 键
     * @return 剩余过期时间（秒），-1 表示永不过期，-2 表示 key 不存在
     */
    fun ttl(key: String): Long

    /**
     * 以毫秒为单位返回 key 的剩余过期时间
     * @param key 键
     * @return 剩余过期时间（毫秒），-1 表示永不过期，-2 表示 key 不存在
     */
    fun pttl(key: String): Long

    /**
     * 查找所有符合给定模式的 key
     * @param pattern 模式
     * @return 符合模式的 key 列表
     */
    fun keys(pattern: String): Set<String>

    /**
     * 返回 key 所储存的值的类型
     * @param key 键
     * @return 类型（none, string, list, set, zset, hash）
     */
    fun type(key: String): String
}
