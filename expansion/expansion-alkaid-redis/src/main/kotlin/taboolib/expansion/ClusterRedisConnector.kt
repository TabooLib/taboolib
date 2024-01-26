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

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.Connection
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisCluster
import java.io.Closeable

class ClusterRedisConnector : Closeable {

    var auth: String? = null
    var pass: String? = null
    var connect = 32
    var timeout = 1000
    var reconnectDelay = 1000L
    var maxAttempts = 20
    var clientName: String = "default"

    lateinit var cluster: JedisCluster
    val nodes: LinkedHashSet<HostAndPort> = linkedSetOf()
    val genericObjectPoolConfig = GenericObjectPoolConfig<Connection>()


    fun build(): ClusterRedisConnector {
        genericObjectPoolConfig.maxTotal = connect
        cluster = if (auth != null && pass != null) {
            JedisCluster(nodes, timeout, timeout, maxAttempts, auth, pass, clientName, genericObjectPoolConfig)
        } else {
            JedisCluster(nodes, timeout, timeout, genericObjectPoolConfig)
        }
        return this
    }

    /**
     * 关闭连接
     */
    override fun close() {
        cluster.close()
    }

    /**
     * 获取 Redis 连接
     *
     * @return [ClusterRedisConnection]
     */
    fun connection(): ClusterRedisConnection {
        return ClusterRedisConnection(this)
    }

    fun connection(action: ClusterRedisConnection.() -> Unit): ClusterRedisConnection {
        return ClusterRedisConnection(this).apply {
            action.invoke(this)
        }
    }

    /**
     *  添加链接
     *  链接写法: 127.0.0.1:6379
     */
    fun addNode(node: String): ClusterRedisConnector {
        val split = node.split(":")
        nodes.add(HostAndPort(split[0], split[1].toInt()))
        return this
    }
}
