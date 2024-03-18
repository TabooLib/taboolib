package taboolib.expansion.client.impl

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
import taboolib.expansion.client.PoolType
import taboolib.expansion.URIBuilder
import taboolib.expansion.client.IPubSubConnection
import taboolib.expansion.client.IRedisClient
import java.io.Closeable
import java.time.Duration
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap


class ClusterClient(
    val redisURI: URIBuilder,
    val pool: PoolType,
) : IRedisClient, IPubSubConnection, Closeable {

    private val clusterMap = ConcurrentHashMap<String, Pair<String, Int>>()

    lateinit var connection: StatefulRedisClusterConnection<String, String>
    lateinit var clusterClient: RedisClusterClient

    var poolObj: GenericObjectPool<StatefulRedisClusterConnection<String, String>>? = null
    var asyncPool: CompletionStage<BoundedAsyncPool<StatefulRedisClusterConnection<String, String>>>? = null

    // 每个节点的名称 使用 名称 to 地址设置 后续可以通过名称来查询 更方便操作
    fun connect(vararg uriBuilder: Pair<String, RedisURI>) {
        val list = uriBuilder.toMutableList()
        list.add(0, "default" to redisURI.getURI())
        list.forEach {
            clusterMap[it.first] = it.second.toURI().let { uri -> uri.host to uri.port }
        }
        clusterClient = RedisClusterClient.create(list.map { it.second })
        when (pool) {
            PoolType.NONE -> {
                connection = clusterClient.connect()
            }

            PoolType.SYNC -> {
                poolObj = ConnectionPoolSupport.createGenericObjectPool(
                    { clusterClient.connect() },
                    GenericObjectPoolConfig<StatefulRedisClusterConnection<String, String>>().apply {
                        // 数据类型不相同 偷懒节省一个对象
                        val form = redisURI.poolTwoConfig
                        maxTotal = form.maxTotal
                        maxIdle = form.maxIdle
                        minIdle = form.minIdle
                    }
                )
                connection = poolObj?.borrowObject() ?: error("Connection pool is empty.")
            }

            PoolType.ASYNC -> {
                asyncPool = AsyncConnectionPoolSupport.createBoundedObjectPoolAsync(
                    { clusterClient.connectAsync(StringCodec.UTF8) }, redisURI.poolLettuceConfig.build()
                )
            }
        }
    }

    fun getSubCluster(host: String, port: Int): StatefulRedisConnection<String, String>? {
        return connection.getConnection(host, port)
    }

    fun getSubCluster(name: String): StatefulRedisConnection<String, String>? {
        return clusterMap[name]?.let { (host, port) -> connection.getConnection(host, port) }
    }


    fun enableTopologyRefresh(time: Duration) {
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
    fun enableAdaptiveTopologyRefresh(time: Duration) {
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

    fun async(block: RedisAdvancedClusterAsyncCommands<String, String>.() -> Any): Any {
        return connection.async().let(block)
    }

    fun async(host: String, port: Int, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return connection.getConnection(host, port).let(block)
    }

    fun async(name: String, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return clusterMap[name]?.let { (host, port) -> connection.getConnection(host, port) }?.let(block)
            ?: error("Cluster $name not found.")
    }

    fun sync(block: RedisAdvancedClusterCommands<String, String>.() -> Any): Any {
        return connection.sync().let(block)
    }

    fun sync(host: String, port: Int, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return connection.getConnection(host, port).let(block)
    }

    fun sync(name: String, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return clusterMap[name]?.let { (host, port) -> connection.getConnection(host, port) }?.let(block)
            ?: error("Cluster $name not found.")
    }

    fun reactive(block: RedisAdvancedClusterReactiveCommands<String, String>.() -> Any): Any {
        return connection.reactive().let(block)
    }

    fun reactive(host: String, port: Int, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return connection.getConnection(host, port).let(block)
    }

    fun reactive(name: String, block: StatefulRedisConnection<String, String>.() -> Any): Any {
        return clusterMap[name]?.let { (host, port) -> connection.getConnection(host, port) }?.let(block)
            ?: error("Cluster $name not found.")
    }

    override fun close() {
        poolObj?.close()
        connection.close()
        if (pool == PoolType.ASYNC) {
            asyncPool?.thenAccept {
                it.closeAsync()
                it.close()
            }
            clusterClient.shutdownAsync()
        } else {
            clusterClient.shutdown()
        }
    }

    override fun subscribeSync(vararg channels: String) {
        clusterClient.connectPubSub().sync().subscribe(*channels)
    }

    override fun subscribeAsync(vararg channels: String) {
        clusterClient.connectPubSub().async().subscribe(*channels)
    }

    override fun subscribeReactive(vararg channels: String) {
        clusterClient.connectPubSub().reactive().subscribe(*channels).subscribe()
    }

    override fun addListener(action: LettucePubSubListener) {
        clusterClient.connectPubSub().addListener(action)
    }

    override fun removeListener(action: LettucePubSubListener) {
        clusterClient.connectPubSub().removeListener(action)
    }

}