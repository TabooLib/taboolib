package taboolib.expansion.client

import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions.RefreshTrigger
import io.lettuce.core.cluster.RedisClusterClient
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands
import io.lettuce.core.cluster.api.reactive.RedisAdvancedClusterReactiveCommands
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.support.*
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import taboolib.expansion.LettucePubSubListener
import taboolib.expansion.PoolType
import taboolib.expansion.URIBuilder
import java.io.Closeable
import java.time.Duration
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier


open class ClusterClient(
    val redisURI: URIBuilder,
    val pool: PoolType,
) : IRedisClient, IPubSubConnection, Closeable {

    private val clusterMap = ConcurrentHashMap<String, Pair<String, Int>>()

    lateinit var connection: StatefulRedisClusterConnection<String, String>
    lateinit var clusterClient: RedisClusterClient

    var poolObj: GenericObjectPool<StatefulRedisClusterConnection<String, String>>? = null
    var asyncPool: CompletionStage<BoundedAsyncPool<StatefulRedisClusterConnection<String, String>>>? = null

    open fun connect(vararg uriBuilder: Pair<String, RedisURI>) {
        val list = uriBuilder.toMutableList()
        list.add(0, "default" to redisURI.getURI())
        list.forEach {
            clusterMap[it.first] = it.second.toURI().let { uri -> uri.host to uri.port }
        }
        clusterClient = RedisClusterClient.create(list.map { it.second })
        when (pool) {
            PoolType.NONE -> {
                connection = clusterClient.connect()
                connection.addListener { node, message ->

                }
            }

            PoolType.SYNC -> {
                poolObj = ConnectionPoolSupport.createGenericObjectPool(
                    { clusterClient.connect() }, GenericObjectPoolConfig()
                )
                connection = poolObj?.borrowObject() ?: error("Connection pool is empty.")
            }

            PoolType.ASYNC -> {
                asyncPool = AsyncConnectionPoolSupport.createBoundedObjectPoolAsync(
                    { clusterClient.connectAsync(StringCodec.UTF8) }, BoundedPoolConfig.create()
                )
            }
        }
    }

    open fun getSubCluster(host: String, port: Int): StatefulRedisConnection<String, String>? {
        return connection.getConnection(host, port)
    }

    open fun getSubCluster(name: String): StatefulRedisConnection<String, String>? {
        return clusterMap[name]?.let { (host, port) -> connection.getConnection(host, port) }
    }


    open fun enableTopologyRefresh(time: Duration) {
        val topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .enablePeriodicRefresh(time)
            .build()
        clusterClient.setOptions(
            ClusterClientOptions.builder()
                .topologyRefreshOptions(topologyRefreshOptions)
                .build()
        )
    }

    // 启用自适应拓扑更新
    open fun enableAdaptiveTopologyRefresh(time: Duration) {
        val topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .enableAdaptiveRefreshTrigger(RefreshTrigger.MOVED_REDIRECT, RefreshTrigger.PERSISTENT_RECONNECTS)
            .adaptiveRefreshTriggersTimeout(time)
            .build()
        clusterClient.setOptions(
            ClusterClientOptions.builder()
                .topologyRefreshOptions(topologyRefreshOptions)
                .build()
        )
    }

    open fun async(block: RedisAdvancedClusterAsyncCommands<String, String>.() -> Any): Any {
        return connection.async().let(block)
    }

    open fun async(host: String, port: Int, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return connection.getConnection(host, port).let(block)
    }

    open fun async(name: String, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return clusterMap[name]?.let { (host, port) -> connection.getConnection(host, port) }?.let(block)
            ?: error("Cluster $name not found.")
    }

    open fun sync(block: RedisAdvancedClusterCommands<String, String>.() -> Any): Any {
        return connection.sync().let(block)
    }

    open fun sync(host: String, port: Int, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return connection.getConnection(host, port).let(block)
    }

    open fun sync(name: String, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return clusterMap[name]?.let { (host, port) -> connection.getConnection(host, port) }?.let(block)
            ?: error("Cluster $name not found.")
    }

    open fun reactive(block: RedisAdvancedClusterReactiveCommands<String, String>.() -> Any): Any {
        return connection.reactive().let(block)
    }

    open fun reactive(host: String, port: Int, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return connection.getConnection(host, port).let(block)
    }

    open fun reactive(name: String, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return clusterMap[name]?.let { (host, port) -> connection.getConnection(host, port) }?.let(block)
            ?: error("Cluster $name not found.")
    }

    override fun close() {
        poolObj?.close()
        connection.close()
        clusterClient.shutdown()
    }

    override fun addListener(action: LettucePubSubListener) {
        clusterClient.connectPubSub().addListener(action)
    }

    override fun removeListener(action: LettucePubSubListener) {
        clusterClient.connectPubSub().removeListener(action)
    }

}