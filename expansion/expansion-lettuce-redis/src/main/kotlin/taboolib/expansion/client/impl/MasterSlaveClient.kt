package taboolib.expansion.client.impl

import io.lettuce.core.ReadFrom
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.reactive.RedisReactiveCommands
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.masterreplica.MasterReplica
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.support.AsyncConnectionPoolSupport
import io.lettuce.core.support.BoundedAsyncPool
import io.lettuce.core.support.ConnectionPoolSupport
import org.apache.commons.pool2.impl.GenericObjectPool
import taboolib.expansion.LettucePubSubListener
import taboolib.expansion.client.PoolType
import taboolib.expansion.client.PoolType.*
import taboolib.expansion.URIBuilder
import taboolib.expansion.client.IPubSubConnection
import taboolib.expansion.client.IRedisClient
import taboolib.expansion.client.IRedisCommand
import taboolib.expansion.client.IRedisMultiple
import java.io.Closeable

class MasterSlaveClient(
    val redisURI: URIBuilder,
    val pool: PoolType
) : IRedisClient, IRedisMultiple, IRedisCommand, IPubSubConnection, Closeable {

    override lateinit var sync: RedisCommands<String, String>
    override lateinit var async: RedisAsyncCommands<String, String>
    override lateinit var reactive: RedisReactiveCommands<String, String>

    private val client = RedisClient.create(redisURI.getURI())

    lateinit var connection: StatefulRedisMasterReplicaConnection<String, String>

    var poolObj: GenericObjectPool<StatefulRedisConnection<String, String>>? = null
    var asyncPool: BoundedAsyncPool<StatefulRedisConnection<String, String>>? = null

    lateinit var connectionPubSub: StatefulRedisPubSubConnection<String, String>
    override fun connect(vararg uri: RedisURI) {
        // 无论那种池都需要创建一个主从连接
        connection = MasterReplica.connect(client, StringCodec.UTF8, uri.toList())

        when (pool) {
            NONE -> {
                sync = connection.sync()
                async = connection.async()
                reactive = connection.reactive()
            }

            SYNC -> {
                poolObj = ConnectionPoolSupport.createGenericObjectPool({ connection }, redisURI.poolTwoConfig)
                poolObj?.let {
                    val borrowObject = it.borrowObject()
                    sync = borrowObject.sync()
                    async = borrowObject.async()
                    reactive = borrowObject.reactive()
                }
            }

            ASYNC -> {
                // MasterSlaveClient 使用异步连接池是不合理的 但你仍可以使用并操作主连接
                asyncPool = AsyncConnectionPoolSupport.createBoundedObjectPool(
                    { client.connectAsync(StringCodec.UTF8, redisURI.getURI()) }, redisURI.poolLettuceConfig.build()
                )
            }
        }
        connectionPubSub = client.connectPubSub()
    }

    fun readForm(form: ReadFrom) {
        connection.readFrom = form
    }

    override fun close() {
        connection.close()
        poolObj?.close()
        if (pool == ASYNC) {
            asyncPool?.closeAsync()
            client.shutdownAsync()
        } else {
            client.shutdown()
        }
    }

    override fun subscribeSync(vararg channels: String) {
        connectionPubSub.sync().subscribe(*channels)
    }

    override fun subscribeAsync(vararg channels: String) {
        connectionPubSub.async().subscribe(*channels)
    }

    override fun subscribeReactive(vararg channels: String) {
        connectionPubSub.reactive().subscribe(*channels).subscribe()
    }

    override fun addListener(action: LettucePubSubListener) {
        client.connectPubSub().addListener(action)
    }

    override fun removeListener(action: LettucePubSubListener) {
        client.connectPubSub().removeListener(action)
    }

}