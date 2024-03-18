package taboolib.expansion.client.impl

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.reactive.RedisReactiveCommands
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands
import io.lettuce.core.support.AsyncConnectionPoolSupport
import io.lettuce.core.support.BoundedAsyncPool
import io.lettuce.core.support.ConnectionPoolSupport
import org.apache.commons.pool2.impl.GenericObjectPool
import taboolib.expansion.LettucePubSubListener
import taboolib.expansion.URIBuilder
import taboolib.expansion.client.IPubSubConnection
import taboolib.expansion.client.IRedisClient
import taboolib.expansion.client.IRedisCommand
import taboolib.expansion.client.PoolType
import taboolib.expansion.client.PoolType.*
import java.io.Closeable


class SingleClient(
    val redisURI: URIBuilder,
    val pool: PoolType,
) : IRedisClient, IRedisCommand, IPubSubConnection, Closeable {

    override lateinit var sync: RedisCommands<String, String>
    override lateinit var async: RedisAsyncCommands<String, String>
    override lateinit var reactive: RedisReactiveCommands<String, String>

    val client = RedisClient.create(redisURI.getURI())

    var poolObj: GenericObjectPool<StatefulRedisConnection<String, String>>? = null
    var asyncPool: BoundedAsyncPool<StatefulRedisConnection<String, String>>? = null

    var connectionPubSub: StatefulRedisPubSubConnection<String, String>

    init {
        when (pool) {
            NONE -> {
                sync = client.connect().sync()
                async = client.connect().async()
                reactive = client.connect().reactive()
            }

            SYNC -> {
                poolObj = ConnectionPoolSupport.createGenericObjectPool({ client.connect() }, redisURI.poolTwoConfig)
                poolObj?.let {
                    val borrowObject = it.borrowObject()
                    sync = borrowObject.sync()
                    async = borrowObject.async()
                    reactive = borrowObject.reactive()
                }
            }

            ASYNC -> {
                asyncPool = AsyncConnectionPoolSupport.createBoundedObjectPool(
                    { client.connectAsync(StringCodec.UTF8, redisURI.getURI()) }, redisURI.poolLettuceConfig.build()
                )
            }
        }
        connectionPubSub = client.connectPubSub()
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

    override fun close() {
        poolObj?.close()
        if (pool == ASYNC) {
            asyncPool?.closeAsync()
            client.shutdownAsync()
        } else {
            client.shutdown()
        }

    }


}