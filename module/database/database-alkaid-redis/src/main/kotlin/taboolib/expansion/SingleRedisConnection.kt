/*
 * Copyright 2022 Alkaid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package taboolib.expansion

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import redis.clients.jedis.exceptions.JedisConnectionException
import taboolib.common.PrimitiveIO
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class SingleRedisConnection(@Volatile internal var pool: JedisPool, internal val connector: SingleRedisConnector): Closeable, IRedisConnection {

    private val closed = AtomicBoolean(false)
    private val subscriptions = CopyOnWriteArrayList<Closeable>()
    private val service: ExecutorService = Executors.newCachedThreadPool()
    private val reconnectService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    /**
     * 该连接是否已关闭。关闭后所有操作都会抛出异常。
     */
    fun isClosed(): Boolean {
        return closed.get()
    }

    init {
        AlkaidRedis.register(this)
    }

    private fun <T> exec(func: (Jedis) -> T): T {
        check(!closed.get()) { "Redis connection is closed" }
        val currentPool = pool
        return try {
            currentPool.resource.use { func(it) }
        } catch (ex: JedisConnectionException) {
            PrimitiveIO.error("Redis connection failed: ${ex.message}")
            reconnect(currentPool).resource.use { func(it) }
        }
    }

    @Synchronized
    private fun reconnect(failedPool: JedisPool): JedisPool {
        check(!closed.get()) { "Redis connection is closed" }
        if (pool !== failedPool) {
            return pool
        }
        val connectorPool = connector.pool
        if (connectorPool != null && connectorPool !== failedPool) {
            pool = connectorPool
            return connectorPool
        }
        connector.connect()
        return connector.pool!!.also { pool = it }
    }

    override fun eval(script: String, keys: List<String>, args: List<String>): Any? {
        return exec {
            it.eval(script, keys, args)
        }
    }

    override fun eval(script: String, keyC: Int, args: List<String>): Any? {
        return exec {
            it.eval(script, keyC, *args.toTypedArray())
        }
    }

    /**
     * 关闭连接
     */
    override fun close() {
        if (!closed.compareAndSet(false, true)) {
            return
        }
        subscriptions.forEach { runCatching { it.close() } }
        subscriptions.clear()
        reconnectService.shutdownNow()
        service.shutdownNow()
        runCatching {
            synchronized(this) {
                pool.close()
            }
        }
        AlkaidRedis.unregister(this)
    }

    /**
     * 赋值
     *
     * @param key 键
     * @param value 值
     */
    override operator fun set(key: String, value: String?) {
        exec { if (value == null) it.del(key) else it[key] = value }
    }

    override fun setNx(key: String, value: String?) {
        exec { if (value == null) it.del(key) else it.setnx(key, value) }
    }

    /**
     * 取值
     *
     * @param key 键
     * @return 值
     */
    override operator fun get(key: String): String? {
        return exec { it[key] }
    }

    /**
     * 删除
     *
     * @param key 键
     */
    override fun delete(key: String) {
        exec { it.del(key) }
    }

    /**
     * 赋值并设置过期时间
     *
     * @param key 键
     * @param value 值
     */
    override fun expire(key: String, value: Long, timeUnit: TimeUnit) {
        exec { it.expire(key, timeUnit.toSeconds(value)) }
    }

    /**
     * 是否存在
     *
     * @param key
     * @return Boolean
     */
    override fun contains(key: String): Boolean {
        return exec { it.exists(key) }
    }

    /**
     * 推送信息
     *
     * @param channel 频道
     * @param message 消息
     */
    override fun publish(channel: String, message: Any) {
        exec {
            if (message is String) {
                it.publish(channel, message)
            } else {
                it.publish(channel, Configuration.serialize(message, Type.FAST_JSON).toString())
            }
        }
    }

    /**
     * 订阅频道
     *
     * @param channel 频道
     * @param patternMode 频道名称是否为正则模式
     * @param func 信息处理函数
     */
    override fun subscribe(vararg channel: String, patternMode: Boolean, func: RedisMessage.() -> Unit) {
        submitSubscription(channel, patternMode, createPubSub(patternMode, func))
    }

    private fun submitSubscription(channel: Array<out String>, patternMode: Boolean, pubSub: JedisPubSub) {
        if (closed.get()) {
            return
        }
        runCatching {
            service.submit {
                try {
                    exec { jedis ->
                        if (patternMode) {
                            jedis.psubscribe(pubSub, *channel)
                        } else {
                            jedis.subscribe(pubSub, *channel)
                        }
                    }
                } catch (ex: Throwable) {
                    if (!closed.get()) {
                        PrimitiveIO.error("Redis subscription failed: ${ex.message}")
                        runCatching {
                            reconnectService.schedule(
                                { submitSubscription(channel, patternMode, pubSub) },
                                connector.reconnectDelay,
                                TimeUnit.MILLISECONDS
                            )
                        }
                    }
                }
            }
        }
    }

    override fun createPubSub(patternMode: Boolean, func: RedisMessage.() -> Unit): JedisPubSub {
        return object : JedisPubSub() {

            init {
                subscriptions.add(Closeable {
                    if (patternMode) {
                        punsubscribe()
                    } else {
                        unsubscribe()
                    }
                })
            }

            override fun onMessage(ch: String, msg: String) {
                try {
                    func(RedisMessage(ch, msg, this, patternMode))
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun sadd(key: String, vararg value: String): Long {
        return exec { it.sadd(key, *value) }
    }

    override fun srem(key: String, vararg value: String): Long {
        return exec { it.srem(key, *value) }
    }

    override fun scard(key: String): Long {
        return exec { it.scard(key) }
    }

    override fun smembers(key: String): Set<String> {
        return exec { it.smembers(key) }
    }

    override fun sismember(key: String, value: String): Boolean {
        return exec { it.sismember(key, value) }
    }

    override fun incr(key: String): Long {
        return exec { it.incr(key) }
    }

    override fun incrBy(key: String, value: Long): Long {
        return exec { it.incrBy(key, value) }
    }

    override fun incrByFloat(key: String, value: Double): Double {
        return exec { it.incrByFloat(key, value) }
    }

    override fun decr(key: String): Long {
        return exec { it.decr(key) }
    }

    override fun decrBy(key: String, value: Long): Long {
        return exec { it.decrBy(key, value) }
    }

    override fun hset(key: String, field: String, value: String): Long {
        return exec { it.hset(key, field, value) }
    }

    override fun hget(key: String, field: String): String? {
        return exec { it.hget(key, field) }
    }

    override fun hdel(key: String, vararg field: String): Long {
        return exec { it.hdel(key, *field) }
    }

    override fun hgetAll(key: String): Map<String, String> {
        return exec { it.hgetAll(key) }
    }

    override fun hexists(key: String, field: String): Boolean {
        return exec { it.hexists(key, field) }
    }

    override fun hkeys(key: String): Set<String> {
        return exec { it.hkeys(key) }
    }

    override fun hvals(key: String): List<String> {
        return exec { it.hvals(key) }
    }

    override fun hlen(key: String): Long {
        return exec { it.hlen(key) }
    }

    override fun hincrBy(key: String, field: String, value: Long): Long {
        return exec { it.hincrBy(key, field, value) }
    }

    override fun hincrByFloat(key: String, field: String, value: Double): Double {
        return exec { it.hincrByFloat(key, field, value) }
    }

    override fun hmset(key: String, hash: Map<String, String>) {
        exec { it.hmset(key, hash) }
    }

    override fun hmget(key: String, vararg fields: String): List<String?> {
        return exec { it.hmget(key, *fields) }
    }

    override fun lpush(key: String, vararg values: String): Long {
        return exec { it.lpush(key, *values) }
    }

    override fun rpush(key: String, vararg values: String): Long {
        return exec { it.rpush(key, *values) }
    }

    override fun lpop(key: String): String? {
        return exec { it.lpop(key) }
    }

    override fun rpop(key: String): String? {
        return exec { it.rpop(key) }
    }

    override fun lrange(key: String, start: Long, stop: Long): List<String> {
        return exec { it.lrange(key, start, stop) }
    }

    override fun llen(key: String): Long {
        return exec { it.llen(key) }
    }

    override fun lindex(key: String, index: Long): String? {
        return exec { it.lindex(key, index) }
    }

    override fun lset(key: String, index: Long, value: String) {
        exec { it.lset(key, index, value) }
    }

    override fun ttl(key: String): Long {
        return exec { it.ttl(key) }
    }

    override fun pttl(key: String): Long {
        return exec { it.pttl(key) }
    }

    override fun keys(pattern: String): Set<String> {
        return exec { it.keys(pattern) }
    }

    override fun type(key: String): String {
        return exec { it.type(key) }
    }
}
