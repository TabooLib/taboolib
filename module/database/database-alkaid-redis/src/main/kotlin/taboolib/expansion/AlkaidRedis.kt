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

import taboolib.common.Inject
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency

@Inject
@RuntimeDependencies(
    RuntimeDependency(
        "!redis.clients:jedis:4.2.3",
        test = "!redis.clients.jedis_4_2_3.Jedis",
        relocate = ["!redis.clients.jedis", "!redis.clients.jedis_4_2_3",
            "!org.apache.commons.pool2", "!org.apache.commons.pool2_2_11_1"],
        transitive = false
    ),
    RuntimeDependency(
        "!org.slf4j:slf4j-api:1.7.32",
        test = "!org.slf4j.Logger",
        transitive = false
    ),
    RuntimeDependency(
        "!org.apache.commons:commons-pool2:2.11.1",
        test = "!org.apache.commons.pool2_2_11_1.ObjectPool",
        relocate = ["!org.apache.commons.pool2", "!org.apache.commons.pool2_2_11_1"],
        transitive = false
    ),
    RuntimeDependency(
        "!org.json:json:20211205",
        test = "!org.json.JSONObject",
        transitive = false
    ),
    RuntimeDependency(
        "!com.google.code.gson",
        test = "!com.google.gson.Gson",
        transitive = false
    )
)
object AlkaidRedis {

    /**
     * 创建 Redis 连接器
     *
     * @return [SingleRedisConnector]
     */
    fun create(): SingleRedisConnector {
        return SingleRedisConnector()
    }

    /**
     * 创建 RedisCluster 连接器
     * Redis集群模式连接器
     *
     * Node按照官方推荐最小是 6个 3主3从 最低是 3主
     *
     * @return [ClusterRedisConnector]
     */
    fun linkCluster(builder: ClusterRedisConnector.() -> Unit = {}): ClusterRedisConnector {
        return ClusterRedisConnector().apply {
            builder.invoke(this)
            build()
        }
    }

    /**
     * 创建 Redis 连接
     *
     * @return [SingleRedisConnection]
     */
    fun createDefault(connector: (SingleRedisConnector) -> Unit = { }): SingleRedisConnection {
        return create().also(connector).connect().connection()
    }
}
