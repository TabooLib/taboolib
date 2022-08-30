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
import taboolib.common.platform.function.severe
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.Closeable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SingleRedisConnection(internal var pool: JedisPool, internal val connector: SingleRedisConnector): Closeable {

    internal val service: ExecutorService = Executors.newCachedThreadPool()

    internal fun <T> exec(loop: Boolean = false, func: (Jedis) -> T): T {
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

    override fun close() {
        pool.destroy()
    }

    operator fun set(key: String, value: String?) {
        exec { if (value == null) it.del(key) else it[key] = value }
    }

    operator fun get(key: String): String? {
        return exec { it[key] }
    }

    fun delete(key: String) {
        exec { it.del(key) }
    }

    fun expire(key: String, value: Long, timeUnit: TimeUnit) {
        exec { it.expire(key, timeUnit.toSeconds(value)) }
    }

    fun contains(key: String): Boolean {
        return exec { it.exists(key) }
    }

    fun publish(channel: String, message: Any) {
        exec {
            if (message is String) {
                it.publish(channel, message)
            } else {
                it.publish(channel, Configuration.serialize(message, Type.FAST_JSON).toString())
            }
        }
    }

    fun subscribe(vararg channel: String, patternMode: Boolean = false, message: RedisMessage.() -> Unit) {
        service.submit {
            try {
                exec(true) {
                    if (!patternMode) {
                        it.subscribe(createPubSub(message), *channel)
                    } else {
                        it.psubscribe(createPubSub(message), *channel)
                    }
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }

    internal fun createPubSub(message: RedisMessage.() -> Unit): JedisPubSub {
        return object : JedisPubSub() {

            override fun onMessage(ch: String, msg: String) {
                try {
                    message(RedisMessage(ch, msg, this))
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }
        }
    }
}