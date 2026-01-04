package taboolib.expansion

import io.lettuce.core.ClientOptions
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.reactive.RedisReactiveCommands
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.masterreplica.MasterReplica
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands
import io.lettuce.core.resource.DefaultClientResources
import io.lettuce.core.support.AsyncConnectionPoolSupport
import io.lettuce.core.support.BoundedAsyncPool
import io.lettuce.core.support.ConnectionPoolSupport
import org.apache.commons.pool2.impl.GenericObjectPool
import taboolib.common.platform.function.warning
import taboolib.common.util.t
import taboolib.expansion.lettuce.IRedisChannel
import taboolib.expansion.lettuce.IRedisClient
import taboolib.expansion.lettuce.IRedisCommand
import taboolib.expansion.lettuce.IRedisPubSub
import java.util.concurrent.CompletableFuture

@Suppress("DuplicatedCode")
class LettuceRedisClient(val redisConfig: LettuceRedisConfig): IRedisClient, IRedisChannel, IRedisCommand, IRedisPubSub {

    lateinit var client: RedisClient

    lateinit var pool: GenericObjectPool<StatefulRedisConnection<String, String>>
    lateinit var asyncPool: BoundedAsyncPool<StatefulRedisConnection<String, String>>

    lateinit var masterReplicaPool: GenericObjectPool<StatefulRedisMasterReplicaConnection<String, String>>
    lateinit var masterAsyncReplicaPool: BoundedAsyncPool<StatefulRedisMasterReplicaConnection<String, String>>

    lateinit var pubSubConnection: StatefulRedisPubSubConnection<String, String>
    lateinit var resources: DefaultClientResources

    var enabledSlaves = false

    override fun start(autoRelease: Boolean): CompletableFuture<Void> {
        val completableFuture = CompletableFuture<Void>()
        val resource = DefaultClientResources.builder()

        if (redisConfig.ioThreadPoolSize != 0) {
            resource.ioThreadPoolSize(redisConfig.ioThreadPoolSize)
        }
        if (redisConfig.computationThreadPoolSize != 0) {
            resource.computationThreadPoolSize(redisConfig.computationThreadPoolSize)
        }

        val clientOptions = ClientOptions.builder()
            .autoReconnect(redisConfig.autoReconnect)
            .pingBeforeActivateConnection(redisConfig.pingBeforeActivateConnection)

        if (redisConfig.ssl) {
            clientOptions.sslOptions(redisConfig.sslOptions)
        }
        val uri = redisConfig.redisURIBuilder().build()

        resources = resource.build()
        client = RedisClient.create(resources, uri).apply {
            options = clientOptions.build()
        }
        // 连接 pub/sub 通道
        pubSubConnection = client.connectPubSub()

        if (redisConfig.enableSlaves) {
            enabledSlaves = true
            val slaves = redisConfig.slaves

            // 连接同步
            masterReplicaPool = ConnectionPoolSupport.createGenericObjectPool(
                { MasterReplica.connect(client, StringCodec.UTF8, uri).apply {
                    readFrom = slaves.readFrom
                } },
                redisConfig.pool.slavesPoolConfig()
            )
            // 连接异步
            AsyncConnectionPoolSupport.createBoundedObjectPoolAsync(
                { MasterReplica.connectAsync(client, StringCodec.UTF8, uri).whenComplete { v, _ ->
                    v.readFrom = slaves.readFrom
                } },
                redisConfig.asyncPool.poolConfig()
            ).thenAccept {
                masterAsyncReplicaPool = it
                completableFuture.complete(null)
            }
        } else {
            // 连接同步
            pool = ConnectionPoolSupport.createGenericObjectPool(
                { client.connect() },
                redisConfig.pool.poolConfig()
            )
            // 连接异步
            AsyncConnectionPoolSupport.createBoundedObjectPoolAsync(
                { client.connectAsync(StringCodec.UTF8, uri) },
                redisConfig.asyncPool.poolConfig()
            ).thenAccept {
                asyncPool = it
                completableFuture.complete(null)
            }
        }
        if (autoRelease) {
            LettuceRedis.clients += this
        }
        return completableFuture
    }

    override fun stop() {
        pubSubConnection.close()
        if (enabledSlaves) {
            masterAsyncReplicaPool.close()
            masterReplicaPool.close()
        } else {
            asyncPool.close()
            pool.close()
        }
        client.shutdown()
        resources.shutdown()
    }

    override fun <T> useCommands(block: (RedisCommands<String, String>) -> T): T? {
        return useConnection(
            { block(it.sync()) }
        )
    }

    override fun <T> useAsyncCommands(block: (RedisAsyncCommands<String, String>) -> T): CompletableFuture<T?> {
        return useAsyncConnection(
            { block(it.async()) }
        )
    }

    override fun <T> useReactiveCommands(block: (RedisReactiveCommands<String, String>) -> T): CompletableFuture<T?> {
        return useAsyncConnection(
            { block(it.reactive()) }
        )
    }

    override fun <T> usePubSubCommands(block: (RedisPubSubCommands<String, String>) -> T): T? {
        return block(pubSubConnection.sync())
    }

    override fun <T> usePubSubAsyncCommands(block: (RedisPubSubAsyncCommands<String, String>) -> T): T? {
        return block(pubSubConnection.async())
    }

    override fun <T> usePubSubReactiveCommands(block: (RedisPubSubReactiveCommands<String, String>) -> T): T? {
        return block(pubSubConnection.reactive())
    }

    // sync
    override fun <T> useConnection(
        use: ((StatefulRedisConnection<String, String>) -> T)?,
        useCluster: ((StatefulRedisClusterConnection<String, String>) -> T)?
    ): T? {
        if (use == null) return null
        return if (enabledSlaves) {
            val connection = try {
                masterReplicaPool.borrowObject()
            } catch (e: Exception) {
                warning(
                    """
                        获取连接失败 原因：${e.message}。
                        Failed to borrow connection: ${e.message}.
                    """.t()
                )
                return null
            }

            try {
                use(connection)
            } catch (e: Exception) {
                warning(
                    """
                        Redis 操作失败 原因：${e.message}。
                        Redis operation failed: ${e.message}.
                    """.t()
                )
                null
            } finally {
                masterReplicaPool.returnObject(connection)
            }
        } else {
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

            try {
                use(connection)
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
    }

    // async
    override fun <T> useAsyncConnection(
        use: ((StatefulRedisConnection<String, String>) -> T)?,
        useCluster: ((StatefulRedisClusterConnection<String, String>) -> T)?
    ): CompletableFuture<T?> {
        if (use == null) return CompletableFuture.completedFuture(null)
        return if (enabledSlaves) {
            try {
                masterAsyncReplicaPool.acquire().thenApply { obj ->
                    try {
                        use(obj)
                    } catch (e: Throwable) {
                        warning(
                            """
                                Redis 操作失败 原因：${e.message}。
                                Redis operation failed: ${e.message}.
                            """.t()
                        )
                        null
                    } finally {
                        masterAsyncReplicaPool.release(obj)
                    }
                }
            } catch (e: Throwable) {
                warning(
                    """
                        获取连接失败 原因：${e.message}。
                        Failed to acquire connection: ${e.message}.
                    """.t()
                )
                return CompletableFuture.completedFuture(null)
            }
        } else {
            try {
                asyncPool.acquire().thenApply { obj ->
                    try {
                        use(obj)
                    } catch (e: Throwable) {
                        warning(
                            """
                                Redis 操作失败 原因：${e.message}。
                                Redis operation failed: ${e.message}.
                            """.t()
                        )
                        null
                    } finally {
                        asyncPool.release(obj)
                    }
                }
            } catch (e: Throwable) {
                warning(
                    """
                        获取连接失败 原因：${e.message}。
                        Failed to acquire connection: ${e.message}.
                    """.t()
                )
                return CompletableFuture.completedFuture(null)
            }
        }
    }
}