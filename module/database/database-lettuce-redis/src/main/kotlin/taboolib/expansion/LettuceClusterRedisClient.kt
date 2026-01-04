package taboolib.expansion

import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import io.lettuce.core.cluster.RedisClusterClient
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands
import io.lettuce.core.cluster.api.reactive.RedisClusterReactiveCommands
import io.lettuce.core.cluster.api.sync.RedisClusterCommands
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection
import io.lettuce.core.cluster.pubsub.api.async.RedisClusterPubSubAsyncCommands
import io.lettuce.core.cluster.pubsub.api.reactive.RedisClusterPubSubReactiveCommands
import io.lettuce.core.cluster.pubsub.api.sync.RedisClusterPubSubCommands
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.resource.DefaultClientResources
import io.lettuce.core.support.AsyncConnectionPoolSupport
import io.lettuce.core.support.BoundedAsyncPool
import io.lettuce.core.support.ConnectionPoolSupport
import org.apache.commons.pool2.impl.GenericObjectPool
import taboolib.common.platform.function.warning
import taboolib.common.util.t
import taboolib.expansion.lettuce.IRedisChannel
import taboolib.expansion.lettuce.IRedisClient
import taboolib.expansion.lettuce.cluster.IRedisClusterCommand
import taboolib.expansion.lettuce.cluster.IRedisClusterPubSub
import java.util.concurrent.CompletableFuture
import kotlin.collections.plusAssign
import kotlin.time.toJavaDuration

@Suppress("DuplicatedCode")
class LettuceClusterRedisClient(val redisConfig: LettuceRedisConfig): IRedisClient, IRedisChannel, IRedisClusterCommand, IRedisClusterPubSub {

    lateinit var client: RedisClusterClient

    lateinit var pool: GenericObjectPool<StatefulRedisClusterConnection<String, String>>
    lateinit var asyncPool: BoundedAsyncPool<StatefulRedisClusterConnection<String, String>>

    lateinit var pubSubConnection: StatefulRedisClusterPubSubConnection<String, String>
    lateinit var resources: DefaultClientResources

    override fun start(autoRelease: Boolean): CompletableFuture<Void> {
        val completableFuture = CompletableFuture<Void>()
        val resource = DefaultClientResources.builder()

        if (redisConfig.ioThreadPoolSize != 0) {
            resource.ioThreadPoolSize(redisConfig.ioThreadPoolSize)
        }
        if (redisConfig.computationThreadPoolSize != 0) {
            resource.computationThreadPoolSize(redisConfig.computationThreadPoolSize)
        }

        val cluster = redisConfig.cluster

        val uris = cluster.nodes.map {
            it.redisURIBuilder().build()
        }
        val clientOptions = ClusterClientOptions.builder()

        if (redisConfig.ssl) {
            clientOptions.sslOptions(redisConfig.sslOptions)
        }

        val topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .enablePeriodicRefresh(cluster.enablePeriodicRefresh)
            .enableAdaptiveRefreshTrigger(*cluster.enableAdaptiveRefreshTrigger.toTypedArray())
            .refreshTriggersReconnectAttempts(cluster.refreshTriggersReconnectAttempts)
            .dynamicRefreshSources(cluster.dynamicRefreshSources)
            .closeStaleConnections(cluster.closeStaleConnections)

        cluster.adaptiveRefreshTriggersTimeout?.toJavaDuration()?.let { topologyRefreshOptions.adaptiveRefreshTriggersTimeout(it) }
        cluster.refreshPeriod?.toJavaDuration()?.let { topologyRefreshOptions.refreshPeriod(it) }
        clientOptions
            .topologyRefreshOptions(topologyRefreshOptions.build())
            .autoReconnect(redisConfig.autoReconnect)
            .maxRedirects(cluster.maxRedirects)
            .validateClusterNodeMembership(cluster.validateClusterNodeMembership)
            .pingBeforeActivateConnection(redisConfig.pingBeforeActivateConnection)

        resources = resource.build()
        client = RedisClusterClient.create(resources, uris)
        client.setOptions(clientOptions.build())

        // 连接 pub/sub 通道
        pubSubConnection = client.connectPubSub()
        // 连接同步
        pool = ConnectionPoolSupport.createGenericObjectPool(
            { client.connect().apply {
                if (redisConfig.enableSlaves) {
                    val slaves = redisConfig.slaves
                    readFrom = slaves.readFrom
                }
            } },
            redisConfig.pool.clusterPoolConfig()
        )
        // 连接异步
        AsyncConnectionPoolSupport.createBoundedObjectPoolAsync(
            { client.connectAsync(StringCodec.UTF8).whenComplete { v, _ ->
                if (redisConfig.enableSlaves) {
                    val slaves = redisConfig.slaves
                    v.readFrom = slaves.readFrom
                }
            } },
            redisConfig.asyncPool.poolConfig()
        ).thenAccept {
            asyncPool = it
            completableFuture.complete(null)
        }
        if (autoRelease) {
            LettuceRedis.clusterClients += this
        }
        return completableFuture
    }

    override fun stop() {
        pubSubConnection.close()
        asyncPool.close()
        pool.close()
        client.shutdown()
        resources.shutdown()
    }

    override fun <T> useCommands(block: (RedisClusterCommands<String, String>) -> T): T? {
        return useConnection {
            block(it.sync())
        }
    }

    override fun <T> useAsyncCommands(block: (RedisClusterAsyncCommands<String, String>) -> T): CompletableFuture<T?> {
        return useAsyncConnection {
            block(it.async())
        }
    }

    override fun <T> useReactiveCommands(block: (RedisClusterReactiveCommands<String, String>) -> T): CompletableFuture<T?> {
        return useAsyncConnection {
            block(it.reactive())
        }
    }

    override fun <T> useClusterPubSubCommands(block: (RedisClusterPubSubCommands<String, String>) -> T): T? {
        return block(pubSubConnection.sync())
    }

    override fun <T> useClusterPubSubAsyncCommands(block: (RedisClusterPubSubAsyncCommands<String, String>) -> T): T? {
        return block(pubSubConnection.async())
    }

    override fun <T> useClusterPubSubReactiveCommands(block: (RedisClusterPubSubReactiveCommands<String, String>) -> T): T? {
        return block(pubSubConnection.reactive())
    }

    // sync
    override fun <T> useConnection(
        use: ((StatefulRedisConnection<String, String>) -> T)?,
        useCluster: ((StatefulRedisClusterConnection<String, String>) -> T)?
    ): T? {
        if (useCluster == null) return null
        val connection = try {
            pool.borrowObject()
        } catch (e: Exception) {
            warning(
                """
                    获取连接失败 原因：${e.message}。
                    Failed to borrow connection: ${e.message}.
                """.t()
            )
            return null
        }

        return try {
            useCluster(connection)
        } catch (e: Exception) {
            warning(
                """
                    Redis 操作失败 原因：${e.message}。
                    Redis operation failed: ${e.message}.
                """.t()
            )
            null
        } finally {
            pool.returnObject(connection)
        }
    }

    // async
    override fun <T> useAsyncConnection(
        use: ((StatefulRedisConnection<String, String>) -> T)?,
        useCluster: ((StatefulRedisClusterConnection<String, String>) -> T)?
    ): CompletableFuture<T?> {
        if (useCluster == null) return CompletableFuture.completedFuture(null)
        return try {
            asyncPool.acquire().thenApply { connection ->
                try {
                    useCluster(connection)
                } catch (e: Exception) {
                    warning(
                        """
                            Redis 操作失败 原因：${e.message}。
                            Redis operation failed: ${e.message}.
                        """.t()
                    )
                    null
                } finally {
                    asyncPool.release(connection)
                }
            }
        } catch (e: Exception) {
            warning(
                """
                    获取连接失败 原因：${e.message}。
                    Failed to borrow connection: ${e.message}.
                """.t()
            )
            CompletableFuture.completedFuture(null)
        }
    }
}