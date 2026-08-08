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
    private var active = false
    val nodes: LinkedHashSet<HostAndPort> = linkedSetOf()
    val genericObjectPoolConfig = GenericObjectPoolConfig<Connection>()

    /**
     * 由本 connector 产出的连接。
     *
     * 连接关闭时会一并关闭本 connector，因此一个 connector 只应对应一个连接实例，
     * 否则关闭其中之一会让其余连接立即失效。
     */
    private var sharedConnection: ClusterRedisConnection? = null


    @Synchronized
    fun build(): ClusterRedisConnector {
        if (active) {
            cluster.close()
        }
        genericObjectPoolConfig.maxTotal = connect
        cluster = if (auth != null && pass != null) {
            JedisCluster(nodes, timeout, timeout, maxAttempts, auth, pass, clientName, genericObjectPoolConfig)
        } else {
            JedisCluster(nodes, timeout, timeout, genericObjectPoolConfig)
        }
        active = true
        AlkaidRedis.register(this)
        return this
    }

    /**
     * 关闭连接
     */
    @Synchronized
    override fun close() {
        if (active) {
            active = false
            cluster.close()
        }
        AlkaidRedis.unregister(this)
    }

    /**
     * 获取 Redis 连接。
     *
     * 多次调用返回同一个实例——连接关闭时会一并关闭本 connector，
     * 若每次都新建包装对象，关闭其中任意一个都会让其余连接失效。
     *
     * @return [ClusterRedisConnection]
     */
    @Synchronized
    fun connection(): ClusterRedisConnection {
        val existing = sharedConnection
        if (existing != null && !existing.isClosed()) {
            return existing
        }
        return ClusterRedisConnection(this).also { sharedConnection = it }
    }

    fun connection(action: ClusterRedisConnection.() -> Unit): ClusterRedisConnection {
        return connection().apply {
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
