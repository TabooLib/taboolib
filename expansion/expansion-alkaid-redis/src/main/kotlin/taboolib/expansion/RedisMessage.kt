package taboolib.expansion

import redis.clients.jedis.JedisPubSub
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.Closeable

/**
 * TabooLib
 * taboolib.expansion.RedisMessage
 *
 * @author 坏黑
 * @since 2022/8/30 12:11
 */
class RedisMessage(val channel: String, val message: String, internal val pubSub: JedisPubSub): Closeable {

    inline fun <reified T> get(ignoreConstructor: Boolean = false): T {
        return Configuration.deserialize(Configuration.loadFromString(message, Type.FAST_JSON), ignoreConstructor)
    }

    fun <T> get(obj: T, ignoreConstructor: Boolean = false): T {
        return Configuration.deserialize(Configuration.loadFromString(message, Type.FAST_JSON), obj, ignoreConstructor)
    }

    override fun close() {
        pubSub.unsubscribe()
    }
}