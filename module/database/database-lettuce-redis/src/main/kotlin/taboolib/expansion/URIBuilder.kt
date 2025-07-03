package taboolib.expansion

import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.support.BoundedPoolConfig
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import taboolib.expansion.client.LettuceClientConverter
import taboolib.library.configuration.ConfigurationSection
import java.time.Duration

fun buildClient(action: URIBuilder.() -> Unit): LettuceClientConverter {
    return URIBuilder().apply(action).build()
}

fun buildURI(action: URIBuilder.() -> Unit): RedisURI {
    return URIBuilder().apply(action).getURI()
}

class URIBuilder {

    companion object {
        // 因为Redis节点的复杂程度 虽然规定了配置文件应该如何写 但是还是推荐自己定义
        // 这里给出的配置是给单机模式快速创建使用的
        fun formConfig(config: ConfigurationSection): LettuceClientConverter {
            val builder = URIBuilder()
            config.getString("host")?.let { builder.host(it) } ?: error("Host not found.")
            config.getInt("port").let { builder.port(it) }
            config.getString("user")?.let { builder.user(it) }
            config.getString("password")?.let { builder.password(it) }
            config.getBoolean("ssl").let { builder.ssl(it) }
            config.getString("timeout")?.let { builder.timeout(Duration.parse(it)) }
            config.getInt("database").let { builder.database(it) }

            // uri优先级最高会覆盖掉其他配置
            config.getString("uri")?.let { builder.input(it) }

            config.getString("master_slave.master")?.let { builder.setMasterHost(it) }

            // 连接池
            config.getConfigurationSection("pool")?.let {
                builder.maxTotal(it.getInt("maxTotal"))
                builder.maxIdle(it.getInt("maxIdle"))
                builder.minIdle(it.getInt("minIdle"))
                it.getString("maxWait")?.let { maxWait -> builder.maxWait(Duration.parse(maxWait)) }
            }
            return builder.build()
        }
    }

    private var redisURI = RedisURI.builder()

    private var urlObj: RedisURI? = null


    infix fun input(value: String) {
        urlObj = RedisURI.create(value)
    }

    infix fun String.link(port: Int): URIBuilder {
        host(this)
        port(port)
        return this@URIBuilder
    }

    infix fun user(user: String): URIBuilder {
        redisURI.withAuthentication(user, "")
        return this@URIBuilder
    }

    infix fun password(password: String): URIBuilder {
        // 根据规范 密码不应该是String类型 会导致错误的缓存
        redisURI.withPassword(password.toCharArray())
        return this@URIBuilder
    }

    infix fun database(database: Int): URIBuilder {
        redisURI.withDatabase(database)
        return this@URIBuilder
    }

    infix fun timeout(timeout: Duration): URIBuilder {
        redisURI.withTimeout(timeout)
        return this@URIBuilder
    }

    infix fun ssl(ssl: Boolean): URIBuilder {
        redisURI.withSsl(ssl)
        return this@URIBuilder
    }

    fun host(host: String): URIBuilder {
        redisURI.withHost(host)
        return this
    }

    fun port(port: Int): URIBuilder {
        redisURI.withPort(port)
        return this
    }

    infix fun setMasterHost(masterHost: String): URIBuilder {
        redisURI.withSentinelMasterId(masterHost)
        return this
    }

    fun build(): LettuceClientConverter {
        if (urlObj != null) {
            return LettuceClientConverter(this)
        }
        return LettuceClientConverter(this)
    }

    fun getURI(): RedisURI {
        if (urlObj != null) {
            return urlObj!!
        }
        return redisURI.build()
    }


    // pool2的配置文件 在使用 SYNC 阻塞式连接池的时候用到
    var poolTwoConfig = GenericObjectPoolConfig<StatefulRedisConnection<String, String>>()

    // lettuce的连接池配置 在使用 ASYNC 非阻塞连接池的时候用到
    var poolLettuceConfig = BoundedPoolConfig.builder()

    // 默认 8
    infix fun maxTotal(maxTotal: Int): URIBuilder {
        poolTwoConfig.maxTotal = maxTotal
        poolLettuceConfig.maxTotal(maxTotal)
        return this
    }

    // 默认 8
    infix fun maxIdle(maxIdle: Int): URIBuilder {
        poolTwoConfig.maxIdle = maxIdle
        poolLettuceConfig.maxIdle(maxIdle)
        return this
    }

    // 默认 0
    infix fun minIdle(minIdle: Int): URIBuilder {
        poolTwoConfig.minIdle = minIdle
        poolLettuceConfig.minIdle(minIdle)
        return this
    }

    infix fun maxWait(maxWait: Duration): URIBuilder {
        poolTwoConfig.setMaxWait(maxWait)
        // lettuce异步连接池不存在等待时间
        return this
    }


}
