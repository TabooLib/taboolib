package taboolib.expansion.client

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.reactive.RedisReactiveCommands
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.support.AsyncConnectionPoolSupport
import io.lettuce.core.support.BoundedAsyncPool
import io.lettuce.core.support.BoundedPoolConfig
import io.lettuce.core.support.ConnectionPoolSupport
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import taboolib.expansion.LettucePubSubListener
import taboolib.expansion.PoolType
import taboolib.expansion.PoolType.*
import taboolib.expansion.URIBuilder
import java.io.Closeable


open class SingleClient(
    val redisURI: URIBuilder,
    val pool: PoolType,
) : IRedisClient, IRedisCommand, IPubSubConnection, Closeable {

    override lateinit var sync: RedisCommands<String, String>
    override lateinit var async: RedisAsyncCommands<String, String>
    override lateinit var reactive: RedisReactiveCommands<String, String>

    val client = RedisClient.create(redisURI.getURI())

    var poolObj: GenericObjectPool<StatefulRedisConnection<String, String>>? = null
    var asyncPool: BoundedAsyncPool<StatefulRedisConnection<String, String>>? = null

    init {
        when (pool) {
            NONE -> {
                sync = client.connect().sync()
                async = client.connect().async()
                reactive = client.connect().reactive()
            }

            SYNC -> {
                poolObj = ConnectionPoolSupport.createGenericObjectPool({ client.connect() }, GenericObjectPoolConfig())
                poolObj?.let {
                    val borrowObject = it.borrowObject()
                    sync = borrowObject.sync()
                    async = borrowObject.async()
                    reactive = borrowObject.reactive()
                }
            }

            ASYNC -> {
                asyncPool = AsyncConnectionPoolSupport.createBoundedObjectPool(
                    { client.connectAsync(StringCodec.UTF8, redisURI.getURI()) }, BoundedPoolConfig.create()
                )
            }
        }
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