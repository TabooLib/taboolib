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

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.io.Closeable

class SingleRedisConnector: Closeable {

    var host = "127.0.0.1"
    var port = 6379
    var auth: String? = null
    var pass: String? = null
    var connect = 32
    var timeout = 1000
    var reconnectDelay = 1000L

    internal var pool: JedisPool? = null
    internal var config = JedisPoolConfig()

    /**
     * 连接到 Redis
     *
     * @return [SingleRedisConnector]
     */
    fun connect(): SingleRedisConnector {
        config.maxTotal = connect
        pool = when {
            auth != null && pass != null -> JedisPool(config, host, port, timeout, auth, pass)
            auth != null -> JedisPool(config, host, port, timeout, auth, null)
            pass != null -> JedisPool(config, host, port, timeout, pass)
            else -> JedisPool(config, host, port, timeout)
        }
        return this
    }

    /**
     * 关闭连接
     */
    override fun close() {
        pool?.destroy()
    }

    /**
     * 获取 Redis 连接
     *
     * @return [SingleRedisConnection]
     */
    fun connection(): SingleRedisConnection {
        return SingleRedisConnection(pool ?: error("connect first"), this)
    }

    /**
     * 设置 Redis 地址
     *
     * @param host 地址
     * @return [SingleRedisConnector]
     */
    fun host(host: String): SingleRedisConnector {
        this.host = host
        return this
    }

    /**
     * 设置 Redis 端口
     *
     * @param port 端口
     * @return [SingleRedisConnector]
     */
    fun port(port: Int): SingleRedisConnector {
        this.port = port
        return this
    }

    /**
     * 设置 Redis 账户
     *
     * @param auth 账户
     * @return [SingleRedisConnector]
     */
    fun auth(auth: String?): SingleRedisConnector {
        this.auth = auth
        return this
    }

    /**
     * 设置 Redis 密码
     *
     * @param pass 密码
     * @return [SingleRedisConnector]
     */
    fun pass(pass: String?): SingleRedisConnector {
        this.pass = pass
        return this
    }

    /**
     * 设置 Redis 连接池大小
     *
     * @param connect 连接池大小
     * @return [SingleRedisConnector]
     */
    fun connect(connect: Int): SingleRedisConnector {
        this.connect = connect
        return this
    }

    /**
     * 设置 Redis 连接超时时间
     *
     * @param timeout 超时时间
     * @return [SingleRedisConnector]
     */
    fun timeout(timeout: Int): SingleRedisConnector {
        this.timeout = timeout
        return this
    }

    /**
     * 设置 Redis 重连延迟
     *
     * @param reconnectDelay 重连延迟
     * @return [SingleRedisConnector]
     */
    fun reconnectDelay(reconnectDelay: Long): SingleRedisConnector {
        this.reconnectDelay = reconnectDelay
        return this
    }
}