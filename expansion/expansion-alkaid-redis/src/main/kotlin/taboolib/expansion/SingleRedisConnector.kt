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
    var connect = 32
    var timeout = 1000
    var reconnectDelay = 1000L

    internal var pool: JedisPool? = null
    internal var config = JedisPoolConfig()

    fun connect(): SingleRedisConnector {
        config.maxTotal = connect
        pool = if (auth != null) JedisPool(config, host, port, timeout, auth) else JedisPool(config, host, port, timeout)
        return this
    }

    override fun close() {
        pool?.destroy()
    }

    fun connection(): SingleRedisConnection {
        return SingleRedisConnection(pool ?: error("connect first"), this)
    }

    fun host(host: String): SingleRedisConnector {
        this.host = host
        return this
    }

    fun port(port: Int): SingleRedisConnector {
        this.port = port
        return this
    }

    fun auth(auth: String?): SingleRedisConnector {
        this.auth = auth
        return this
    }

    fun connect(connect: Int): SingleRedisConnector {
        this.connect = connect
        return this
    }

    fun timeout(timeout: Int): SingleRedisConnector {
        this.timeout = timeout
        return this
    }

    fun reconnectDelay(reconnectDelay: Long): SingleRedisConnector {
        this.reconnectDelay = reconnectDelay
        return this
    }
}