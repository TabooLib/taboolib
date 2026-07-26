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
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.plusAssign
import kotlin.time.toJavaDuration

@Suppress("DuplicatedCode")
class LettuceClusterRedisClient(val redisConfig: LettuceRedisConfig): IRedisClient, IRedisChannel, IRedisClusterCommand, IRedisClusterPubSub, LettuceRedisResource {

    @Volatile
    lateinit var client: RedisClusterClient

    @Volatile
    lateinit var pool: GenericObjectPool<StatefulRedisClusterConnection<String, String>>

    @Volatile
    lateinit var asyncPool: BoundedAsyncPool<StatefulRedisClusterConnection<String, String>>

    @Volatile
    lateinit var pubSubConnection: StatefulRedisClusterPubSubConnection<String, String>

    @Volatile
    lateinit var resources: DefaultClientResources

    private val startup = AtomicReference<CompletableFuture<Void>?>()
    private val stopped = AtomicBoolean(false)
    private val shutdownStarted = AtomicBoolean(false)
    private val lifecycleLock = Any()

    @OptIn(ExperimentalStdlibApi::class)
    override fun start(autoRelease: Boolean): CompletableFuture<Void> {
        val completableFuture = CompletableFuture<Void>()
        if (!startup.compareAndSet(null, completableFuture)) {
            val existingStartup = startup.get()!!
            if (stopped.get() && !existingStartup.isCompletedExceptionally && !existingStartup.isCancelled) {
                return CompletableFuture<Void>().also {
                    it.completeExceptionally(CancellationException("Redis cluster client is stopped"))
                }
            }
            return existingStartup
        }
        if (stopped.get()) {
            completableFuture.completeExceptionally(CancellationException("Redis cluster client is stopped"))
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

            val cluster = redisConfig.cluster
            val uris = cluster.nodes.map { it.redisURIBuilder().build() }
            val clientOptions = ClusterClientOptions.builder()
            if (redisConfig.ssl) {
                clientOptions.sslOptions(redisConfig.sslOptions)
            }

            val topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh(cluster.enablePeriodicRefresh)
                .refreshTriggersReconnectAttempts(cluster.refreshTriggersReconnectAttempts)
                .dynamicRefreshSources(cluster.dynamicRefreshSources)
                .closeStaleConnections(cluster.closeStaleConnections)

            // Lettuce 7.0+ 默认启用所有自适应触发器，需要禁用未配置的触发器
            val configuredTriggers = cluster.enableAdaptiveRefreshTrigger.toSet()
            if (configuredTriggers.isEmpty()) {
                topologyRefreshOptions.disableAllAdaptiveRefreshTriggers()
            } else {
                val triggersToDisable = ClusterTopologyRefreshOptions.RefreshTrigger.values()
                    .filter { it !in configuredTriggers }
                    .toTypedArray()
                if (triggersToDisable.isNotEmpty()) {
                    topologyRefreshOptions.disableAdaptiveRefreshTrigger(*triggersToDisable)
                }
            }

            cluster.adaptiveRefreshTriggersTimeout?.toJavaDuration()?.let {
                topologyRefreshOptions.adaptiveRefreshTriggersTimeout(it)
            }
            cluster.refreshPeriod?.toJavaDuration()?.let {
                topologyRefreshOptions.refreshPeriod(it)
            }
            clientOptions
                .topologyRefreshOptions(topologyRefreshOptions.build())
                .autoReconnect(redisConfig.autoReconnect)
                .maxRedirects(cluster.maxRedirects)
                .validateClusterNodeMembership(cluster.validateClusterNodeMembership)
                .pingBeforeActivateConnection(redisConfig.pingBeforeActivateConnection)

            val newResources = resource.build()
            val newClient = try {
                RedisClusterClient.create(newResources, uris).apply {
                    setOptions(clientOptions.build())
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
            val pubSubReady = client.connectPubSubAsync(StringCodec.UTF8).thenAccept {
                pubSubConnection = it
                if (stopped.get()) {
                    it.closeAsync()
                }
            }.toCompletableFuture()
            // 连接同步
            pool = ConnectionPoolSupport.createGenericObjectPool(
                {
                    client.connect().apply {
                        if (redisConfig.enableSlaves) {
                            readFrom = redisConfig.slaves.readFrom
                        }
                    }
                },
                redisConfig.pool.clusterPoolConfig()
            )
            if (stopped.get()) {
                pool.close()
            }
            // 连接异步
            val poolReady = AsyncConnectionPoolSupport.createBoundedObjectPoolAsync(
                {
                    client.connectAsync(StringCodec.UTF8).whenComplete { value, _ ->
                        if (redisConfig.enableSlaves) {
                            value.readFrom = redisConfig.slaves.readFrom
                        }
                    }
                },
                redisConfig.asyncPool.poolConfig()
            ).thenAccept {
                asyncPool = it
                if (stopped.get()) {
                    it.closeAsync()
                }
            }.toCompletableFuture()
            coordinateStart(completableFuture, autoRelease, pubSubReady, poolReady)
        } catch (ex: Throwable) {
            failStart(completableFuture, ex)
        }
        return completableFuture
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun startSync(autoRelease: Boolean) {
        val completableFuture = CompletableFuture<Void>()
        if (!startup.compareAndSet(null, completableFuture)) {
            val existingStartup = startup.get()!!
            check(!stopped.get() && existingStartup.isDone && !existingStartup.isCompletedExceptionally && !existingStartup.isCancelled) {
                "Redis cluster client is already starting or failed to start"
            }
            return
        }
        if (stopped.get()) {
            val error = IllegalStateException("Redis cluster client is stopped")
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

            val cluster = redisConfig.cluster
            val uris = cluster.nodes.map { it.redisURIBuilder().build() }
            val clientOptions = ClusterClientOptions.builder()
            if (redisConfig.ssl) {
                clientOptions.sslOptions(redisConfig.sslOptions)
            }

            val topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh(cluster.enablePeriodicRefresh)
                .refreshTriggersReconnectAttempts(cluster.refreshTriggersReconnectAttempts)
                .dynamicRefreshSources(cluster.dynamicRefreshSources)
                .closeStaleConnections(cluster.closeStaleConnections)

            // Lettuce 7.0+ 默认启用所有自适应触发器，需要禁用未配置的触发器
            val configuredTriggers = cluster.enableAdaptiveRefreshTrigger.toSet()
            if (configuredTriggers.isEmpty()) {
                topologyRefreshOptions.disableAllAdaptiveRefreshTriggers()
            } else {
                val triggersToDisable = ClusterTopologyRefreshOptions.RefreshTrigger.values()
                    .filter { it !in configuredTriggers }
                    .toTypedArray()
                if (triggersToDisable.isNotEmpty()) {
                    topologyRefreshOptions.disableAdaptiveRefreshTrigger(*triggersToDisable)
                }
            }

            cluster.adaptiveRefreshTriggersTimeout?.toJavaDuration()?.let {
                topologyRefreshOptions.adaptiveRefreshTriggersTimeout(it)
            }
            cluster.refreshPeriod?.toJavaDuration()?.let {
                topologyRefreshOptions.refreshPeriod(it)
            }
            clientOptions
                .topologyRefreshOptions(topologyRefreshOptions.build())
                .autoReconnect(redisConfig.autoReconnect)
                .maxRedirects(cluster.maxRedirects)
                .validateClusterNodeMembership(cluster.validateClusterNodeMembership)
                .pingBeforeActivateConnection(redisConfig.pingBeforeActivateConnection)

            val newResources = resource.build()
            val newClient = try {
                RedisClusterClient.create(newResources, uris).apply {
                    setOptions(clientOptions.build())
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
                error("Redis cluster client was stopped during startup")
            }

            // 连接 pub/sub 通道
            pubSubConnection = client.connectPubSub()
            if (stopped.get()) {
                pubSubConnection.closeAsync()
                error("Redis cluster client was stopped during startup")
            }
            // 连接同步
            pool = ConnectionPoolSupport.createGenericObjectPool(
                {
                    client.connect().apply {
                        if (redisConfig.enableSlaves) {
                            readFrom = redisConfig.slaves.readFrom
                        }
                    }
                },
                redisConfig.pool.clusterPoolConfig()
            )
            if (stopped.get()) {
                pool.close()
                error("Redis cluster client was stopped during startup")
            }
            // 连接异步（同步方式创建）
            asyncPool = AsyncConnectionPoolSupport.createBoundedObjectPool(
                {
                    client.connectAsync(StringCodec.UTF8).whenComplete { value, _ ->
                        if (redisConfig.enableSlaves) {
                            value.readFrom = redisConfig.slaves.readFrom
                        }
                    }
                },
                redisConfig.asyncPool.poolConfig()
            )
            if (stopped.get()) {
                asyncPool.closeAsync()
                error("Redis cluster client was stopped during startup")
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
        startup.get()?.completeExceptionally(CancellationException("Redis cluster client is stopped"))
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
            completableFuture.completeExceptionally(CancellationException("Redis cluster client is stopped"))
            closeResources()
            return
        }
        if (autoRelease) {
            LettuceRedis.register(this)
        }
        if (stopped.get()) {
            LettuceRedis.unregister(this)
            completableFuture.completeExceptionally(CancellationException("Redis cluster client is stopped"))
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
        if (::asyncPool.isInitialized) {
            runCatching { closing += asyncPool.closeAsync() }
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
        return block(requirePubSubConnection().sync())
    }

    override fun <T> useClusterPubSubAsyncCommands(block: (RedisClusterPubSubAsyncCommands<String, String>) -> T): T? {
        return block(requirePubSubConnection().async())
    }

    override fun <T> useClusterPubSubReactiveCommands(block: (RedisClusterPubSubReactiveCommands<String, String>) -> T): T? {
        return block(requirePubSubConnection().reactive())
    }

    /**
     * 获取 pub/sub 连接，未就绪时给出明确错误。
     *
     * [start] 中的 pub/sub 连接是异步建立的，调用方必须等待 [start] 返回的 future 完成，
     * 否则这里会以清晰的错误信息失败，而不是抛出难以定位的 UninitializedPropertyAccessException。
     */
    private fun requirePubSubConnection(): StatefulRedisClusterPubSubConnection<String, String> {
        check(!stopped.get()) {
            """
                Redis 集群客户端已停止，无法使用 pub/sub 连接。
                Redis cluster client is stopped, pub/sub connection is unavailable.
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