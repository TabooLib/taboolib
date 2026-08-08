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
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@Suppress("DuplicatedCode")
class LettuceRedisClient(val redisConfig: LettuceRedisConfig): IRedisClient, IRedisChannel, IRedisCommand, IRedisPubSub, LettuceRedisResource {

    @Volatile
    lateinit var client: RedisClient

    @Volatile
    lateinit var pool: GenericObjectPool<StatefulRedisConnection<String, String>>

    @Volatile
    lateinit var asyncPool: BoundedAsyncPool<StatefulRedisConnection<String, String>>

    @Volatile
    lateinit var masterReplicaPool: GenericObjectPool<StatefulRedisMasterReplicaConnection<String, String>>

    @Volatile
    lateinit var masterAsyncReplicaPool: BoundedAsyncPool<StatefulRedisMasterReplicaConnection<String, String>>

    @Volatile
    lateinit var pubSubConnection: StatefulRedisPubSubConnection<String, String>

    @Volatile
    lateinit var resources: DefaultClientResources

    var enabledSlaves = false

    private val startup = AtomicReference<CompletableFuture<Void>?>()
    private val stopped = AtomicBoolean(false)
    private val shutdownStarted = AtomicBoolean(false)
    private val lifecycleLock = Any()

    override fun start(autoRelease: Boolean): CompletableFuture<Void> {
        val completableFuture = CompletableFuture<Void>()
        if (!startup.compareAndSet(null, completableFuture)) {
            val existingStartup = startup.get()!!
            if (stopped.get() && !existingStartup.isCompletedExceptionally && !existingStartup.isCancelled) {
                return CompletableFuture<Void>().also {
                    it.completeExceptionally(CancellationException("Redis client is stopped"))
                }
            }
            return existingStartup
        }
        if (stopped.get()) {
            completableFuture.completeExceptionally(CancellationException("Redis client is stopped"))
            return completableFuture
        }
        try {
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

            val newResources = resource.build()
            val newClient = try {
                RedisClient.create(newResources, uri).apply {
                    options = clientOptions.build()
                }
            } catch (ex: Throwable) {
                newResources.shutdown()
                throw ex
            }
            synchronized(lifecycleLock) {
                resources = newResources
                client = newClient
            }
            if (stopped.get()) {
                closeResources()
                return completableFuture
            }
            // 异步连接 pub/sub 通道，避免 start() 阻塞调用线程
            val pubSubReady = client.connectPubSubAsync(StringCodec.UTF8, uri).thenAccept {
                pubSubConnection = it
                if (stopped.get()) {
                    it.closeAsync()
                }
            }.toCompletableFuture()

            if (redisConfig.enableSlaves) {
                enabledSlaves = true
                val slaves = redisConfig.slaves
                // 连接同步
                masterReplicaPool = ConnectionPoolSupport.createGenericObjectPool(
                    {
                        MasterReplica.connect(client, StringCodec.UTF8, uri).apply {
                            readFrom = slaves.readFrom
                        }
                    },
                    redisConfig.pool.slavesPoolConfig()
                )
                if (stopped.get()) {
                    masterReplicaPool.close()
                }
                // 连接异步
                val poolReady = AsyncConnectionPoolSupport.createBoundedObjectPoolAsync(
                    {
                        MasterReplica.connectAsync(client, StringCodec.UTF8, uri).whenComplete { value, _ ->
                            value.readFrom = slaves.readFrom
                        }
                    },
                    redisConfig.asyncPool.poolConfig()
                ).thenAccept {
                    masterAsyncReplicaPool = it
                    if (stopped.get()) {
                        it.closeAsync()
                    }
                }.toCompletableFuture()
                coordinateStart(completableFuture, autoRelease, pubSubReady, poolReady)
            } else {
                // 连接同步
                pool = ConnectionPoolSupport.createGenericObjectPool(
                    { client.connect() },
                    redisConfig.pool.poolConfig()
                )
                if (stopped.get()) {
                    pool.close()
                }
                // 连接异步
                val poolReady = AsyncConnectionPoolSupport.createBoundedObjectPoolAsync(
                    { client.connectAsync(StringCodec.UTF8, uri) },
                    redisConfig.asyncPool.poolConfig()
                ).thenAccept {
                    asyncPool = it
                    if (stopped.get()) {
                        it.closeAsync()
                    }
                }.toCompletableFuture()
                coordinateStart(completableFuture, autoRelease, pubSubReady, poolReady)
            }
        } catch (ex: Throwable) {
            failStart(completableFuture, ex)
        }
        return completableFuture
    }

    override fun startSync(autoRelease: Boolean) {
        val completableFuture = CompletableFuture<Void>()
        if (!startup.compareAndSet(null, completableFuture)) {
            val existingStartup = startup.get()!!
            check(!stopped.get() && existingStartup.isDone && !existingStartup.isCompletedExceptionally && !existingStartup.isCancelled) {
                "Redis client is already starting or failed to start"
            }
            return
        }
        if (stopped.get()) {
            val error = IllegalStateException("Redis client is stopped")
            completableFuture.completeExceptionally(error)
            throw error
        }
        try {
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

            val newResources = resource.build()
            val newClient = try {
                RedisClient.create(newResources, uri).apply {
                    options = clientOptions.build()
                }
            } catch (ex: Throwable) {
                newResources.shutdown()
                throw ex
            }
            synchronized(lifecycleLock) {
                resources = newResources
                client = newClient
            }
            if (stopped.get()) {
                closeResources()
                error("Redis client was stopped during startup")
            }
            // 连接 pub/sub 通道
            pubSubConnection = client.connectPubSub()
            if (stopped.get()) {
                pubSubConnection.closeAsync()
                error("Redis client was stopped during startup")
            }

            if (redisConfig.enableSlaves) {
                enabledSlaves = true
                val slaves = redisConfig.slaves
                // 连接同步
                masterReplicaPool = ConnectionPoolSupport.createGenericObjectPool(
                    {
                        MasterReplica.connect(client, StringCodec.UTF8, uri).apply {
                            readFrom = slaves.readFrom
                        }
                    },
                    redisConfig.pool.slavesPoolConfig()
                )
                if (stopped.get()) {
                    masterReplicaPool.close()
                    error("Redis client was stopped during startup")
                }
                // 连接异步（同步方式创建）
                masterAsyncReplicaPool = AsyncConnectionPoolSupport.createBoundedObjectPool(
                    {
                        MasterReplica.connectAsync(client, StringCodec.UTF8, uri).whenComplete { value, _ ->
                            value.readFrom = slaves.readFrom
                        }
                    },
                    redisConfig.asyncPool.poolConfig()
                )
                if (stopped.get()) {
                    masterAsyncReplicaPool.closeAsync()
                    error("Redis client was stopped during startup")
                }
            } else {
                // 连接同步
                pool = ConnectionPoolSupport.createGenericObjectPool(
                    { client.connect() },
                    redisConfig.pool.poolConfig()
                )
                if (stopped.get()) {
                    pool.close()
                    error("Redis client was stopped during startup")
                }
                // 连接异步（同步方式创建）
                asyncPool = AsyncConnectionPoolSupport.createBoundedObjectPool(
                    { client.connectAsync(StringCodec.UTF8, uri) },
                    redisConfig.asyncPool.poolConfig()
                )
                if (stopped.get()) {
                    asyncPool.closeAsync()
                    error("Redis client was stopped during startup")
                }
            }
            completeStart(completableFuture, autoRelease)
        } catch (ex: Throwable) {
            failStart(completableFuture, ex)
            throw ex
        }
    }

    override fun stop() {
        if (!stopped.compareAndSet(false, true)) {
            return
        }
        LettuceRedis.unregister(this)
        startup.get()?.completeExceptionally(CancellationException("Redis client is stopped"))
        closeResources()
    }

    private fun coordinateStart(
        completableFuture: CompletableFuture<Void>,
        autoRelease: Boolean,
        vararg stages: CompletableFuture<*>
    ) {
        val coordinator = AsyncStartupCoordinator(
            stages.size,
            onSuccess = { completeStart(completableFuture, autoRelease) },
            onFailure = { failStart(completableFuture, it) },
            onSettled = { if (stopped.get()) closeResources() },
        )
        stages.forEach { stage ->
            stage.whenComplete { _, error -> coordinator.complete(error) }
        }
    }

    private fun completeStart(completableFuture: CompletableFuture<Void>, autoRelease: Boolean) {
        if (stopped.get()) {
            completableFuture.completeExceptionally(CancellationException("Redis client is stopped"))
            closeResources()
            return
        }
        if (autoRelease) {
            LettuceRedis.register(this)
        }
        if (stopped.get()) {
            LettuceRedis.unregister(this)
            completableFuture.completeExceptionally(CancellationException("Redis client is stopped"))
            closeResources()
            return
        }
        completableFuture.complete(null)
    }

    private fun failStart(completableFuture: CompletableFuture<Void>, error: Throwable) {
        stopped.set(true)
        LettuceRedis.unregister(this)
        completableFuture.completeExceptionally(error)
        closeResources()
    }

    private fun closeResources() {
        val (clientToClose, resourcesToClose) = synchronized(lifecycleLock) {
            val currentClient = if (::client.isInitialized) client else null
            val currentResources = if (::resources.isInitialized) resources else null
            currentClient to currentResources
        }
        if (clientToClose == null || !shutdownStarted.compareAndSet(false, true)) {
            return
        }
        val closing = ArrayList<CompletableFuture<*>>()
        if (::pubSubConnection.isInitialized) {
            runCatching { closing += pubSubConnection.closeAsync() }
        }
        if (::masterAsyncReplicaPool.isInitialized) {
            runCatching { closing += masterAsyncReplicaPool.closeAsync() }
        }
        if (::asyncPool.isInitialized) {
            runCatching { closing += asyncPool.closeAsync() }
        }
        if (::masterReplicaPool.isInitialized) {
            runCatching { masterReplicaPool.close() }
        }
        if (::pool.isInitialized) {
            runCatching { pool.close() }
        }
        val connectionsClosed = if (closing.isEmpty()) {
            CompletableFuture.completedFuture(null)
        } else {
            CompletableFuture.allOf(*closing.toTypedArray())
        }
        connectionsClosed.handle { _, _ -> null }.thenCompose {
            clientToClose.shutdownAsync()
        }.whenComplete { _, _ ->
            runCatching { resourcesToClose?.shutdown() }
        }
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
        return block(requirePubSubConnection().sync())
    }

    override fun <T> usePubSubAsyncCommands(block: (RedisPubSubAsyncCommands<String, String>) -> T): T? {
        return block(requirePubSubConnection().async())
    }

    override fun <T> usePubSubReactiveCommands(block: (RedisPubSubReactiveCommands<String, String>) -> T): T? {
        return block(requirePubSubConnection().reactive())
    }

    /**
     * 获取 pub/sub 连接，未就绪时给出明确错误。
     *
     * [start] 中的 pub/sub 连接是异步建立的，调用方必须等待 [start] 返回的 future 完成，
     * 否则这里会以清晰的错误信息失败，而不是抛出难以定位的 UninitializedPropertyAccessException。
     */
    private fun requirePubSubConnection(): StatefulRedisPubSubConnection<String, String> {
        check(!stopped.get()) {
            """
                Redis 客户端已停止，无法使用 pub/sub 连接。
                Redis client is stopped, pub/sub connection is unavailable.
            """.t()
        }
        check(::pubSubConnection.isInitialized) {
            """
                pub/sub 连接尚未就绪，请先等待 start() 返回的 CompletableFuture 完成，或改用 startSync()。
                The pub/sub connection is not ready yet, await the CompletableFuture returned by start() or use startSync() instead.
            """.t()
        }
        return pubSubConnection
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