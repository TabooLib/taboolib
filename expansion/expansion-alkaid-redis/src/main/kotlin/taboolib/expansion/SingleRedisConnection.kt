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
import redis.clients.jedis.params.ScanParams
import redis.clients.jedis.resps.ScanResult
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.severe
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SingleRedisConnection(internal var pool: JedisPool, internal val connector: SingleRedisConnector): Closeable, IRedisConnection,IChannel {

    private val service: ExecutorService = Executors.newCachedThreadPool()

    private fun <T> exec(loop: Boolean = false, func: (Jedis) -> T): T {
        return try {
            pool.resource.use { func(it) }
        } catch (ex: JedisConnectionException) {
            severe("Redis connection failed: ${ex.message}")
            // 如果是循环模式则等待一段时间
            if (loop) {
                Thread.sleep(connector.reconnectDelay)
            }
            // 重连
            pool = connector.connect().pool!!
            // 重新执行
            if (loop) {
                exec(true, func)
            } else {
                pool.resource.use { func(it) }
            }
        }
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
        pool.destroy()
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
     * @param seconds 过期时间
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

    override fun getList(key: String): Map<String, String> {
        return exec {
            var cursor = "0"
            val scanParams = ScanParams().match(key)
            val resultMap: MutableMap<String, String> = HashMap()

            do {
                val scanResult: ScanResult<String> = it.scan(cursor, scanParams)
                val partialKeys = scanResult.result
                for (partialKey in partialKeys) {
                    val value = it.get(partialKey) ?: ""
                    resultMap[partialKey] = value
                }
                cursor = scanResult.cursor
            } while (cursor != "0")
            resultMap
        }
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
        service.submit {
            try {
                exec(true) { jedis ->
                    if (patternMode) {
                        jedis.psubscribe(createPubSub(true, func), *channel)
                    } else {
                        jedis.subscribe(createPubSub(false, func), *channel)
                    }
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }

    override fun createPubSub(patternMode: Boolean, func: RedisMessage.() -> Unit): JedisPubSub {
        return object : JedisPubSub() {

            init {
                resources.add(Closeable {
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

    companion object {

        val resources = CopyOnWriteArrayList<Closeable>()

        @Awake(LifeCycle.DISABLE)
        private fun onDisable() {
            resources.forEach { kotlin.runCatching { it.close() } }
        }
    }
}
