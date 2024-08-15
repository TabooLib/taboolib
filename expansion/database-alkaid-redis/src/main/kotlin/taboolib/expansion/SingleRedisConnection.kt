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
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.platform.Awake
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SingleRedisConnection(internal var pool: JedisPool, internal val connector: SingleRedisConnector): Closeable, IRedisConnection {

    private val service: ExecutorService = Executors.newCachedThreadPool()

    private fun <T> exec(loop: Boolean = false, func: (Jedis) -> T): T {
        return try {
            pool.resource.use { func(it) }
        } catch (ex: JedisConnectionException) {
            PrimitiveIO.error("Redis connection failed: ${ex.message}")
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

    override fun close() {
        pool.destroy()
    }

    override fun set(key: String, value: String?) {
        exec { if (value == null) it.del(key) else it[key] = value }
    }

    override fun setNx(key: String, value: String?) {
        exec { if (value == null) it.del(key) else it.setnx(key, value) }
    }

    override fun get(key: String): String? {
        return exec { it[key] }
    }

    override fun delete(key: String) {
        exec { it.del(key) }
    }

    override fun expire(key: String, value: Long, timeUnit: TimeUnit) {
        exec { it.expire(key, timeUnit.toSeconds(value)) }
    }

    override fun contains(key: String): Boolean {
        return exec { it.exists(key) }
    }

    override fun publish(channel: String, message: Any) {
        exec {
            if (message is String) {
                it.publish(channel, message)
            } else {
                it.publish(channel, Configuration.serialize(message, Type.FAST_JSON).toString())
            }
        }
    }

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

    // 实现 hset 方法
    override fun hset(key: String, field: String, value: String) {
        exec { it.hset(key, field, value) }
    }

    // 实现 hget 方法
    override fun hget(key: String, field: String): String? {
        return exec { it.hget(key, field) }
    }

    // 实现 hdel 方法
    override fun hdel(key: String, vararg fields: String) {
        exec { it.hdel(key, *fields) }
    }

    // 实现 hexists 方法
    override fun hexists(key: String, field: String): Boolean {
        return exec { it.hexists(key, field) }
    }

    @Inject
    internal companion object {

        val resources = CopyOnWriteArrayList<Closeable>()

        @Awake(LifeCycle.DISABLE)
        private fun onDisable() {
            resources.forEach { runCatching { it.close() } }
        }
    }
}

