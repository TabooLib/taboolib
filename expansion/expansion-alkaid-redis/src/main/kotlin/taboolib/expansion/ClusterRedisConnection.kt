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

import redis.clients.jedis.JedisPubSub
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ClusterRedisConnection(val connector: ClusterRedisConnector) : Closeable, IRedisConnection,IChannel {

    private val service: ExecutorService = Executors.newCachedThreadPool()

    companion object {

        val resources = CopyOnWriteArrayList<Closeable>()

        @Awake(LifeCycle.DISABLE)
        private fun onDisable() {
            resources.forEach { kotlin.runCatching { it.close() } }
        }
    }

    override fun eval(script: String, keys: List<String>, args: List<String>): Any? {
        return connector.cluster.eval(script, keys, args)
    }

    override fun eval(script: String, keyC: Int, args: List<String>): Any? {
        return connector.cluster.eval(script, keyC, *args.toTypedArray())
    }

    override fun close() {
        connector.close()
        service.shutdown()
        service.awaitTermination(30, TimeUnit.SECONDS)
    }

    override fun set(key: String, value: String?) {
        if (value == null) {
            delete(key)
            return
        }
        connector.cluster.set(key, value)
    }

    override fun setNx(key: String, value: String?) {
        if (value == null) {
            delete(key)
            return
        }
        connector.cluster.setnx(key, value)
    }

    override fun setEx(key: String, value: String?, seconds: Long, timeUnit: TimeUnit) {
        if (value == null) {
            delete(key)
            return
        }
        connector.cluster.setex(key, timeUnit.toSeconds(seconds), value)
    }

    override fun get(key: String): String? {
        return connector.cluster.get(key)
    }

    override fun delete(key: String) {
        connector.cluster.del(key)
    }

    override fun expire(key: String, value: Long, timeUnit: TimeUnit) {
        connector.cluster.expire(key, timeUnit.toSeconds(value))
    }

    override fun contains(key: String): Boolean {
        return connector.cluster.exists(key)
    }

    override fun publish(channel: String, message: Any) {
        if (message is String) {
            connector.cluster.publish(channel, message)
        } else {
            connector.cluster.publish(channel, Configuration.serialize(message, Type.FAST_JSON).toString())
        }

    }

    override fun subscribe(vararg channel: String, patternMode: Boolean, func: RedisMessage.() -> Unit) {
        service.submit {
            try {
                if (patternMode) {
                    connector.cluster.psubscribe(createPubSub(true, func), *channel)
                } else {
                    connector.cluster.subscribe(createPubSub(false, func), *channel)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
}
