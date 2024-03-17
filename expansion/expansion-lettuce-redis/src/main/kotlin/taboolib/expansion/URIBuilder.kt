package taboolib.expansion

import io.lettuce.core.RedisURI
import io.lettuce.core.pubsub.RedisPubSubListener
import org.reactivestreams.Publisher
import reactor.core.scheduler.Schedulers
import taboolib.expansion.client.ClusterClient
import taboolib.expansion.client.LettuceClientConverter
import taboolib.expansion.client.MasterSlaveClient
import taboolib.expansion.client.SingleClient
import taboolib.library.configuration.ConfigurationSection
import java.time.Duration


fun test() {
    val client = buildClient {
        "localhost" link 6379 password "password" ssl true
        // or
        host("localhost")
        port(6379)
        password("password")
        ssl(true)
    }.link<SingleClient>(pool = PoolType.SYNC) {
        // do something

        val listener = pubSubListener {
            onSubscribed = { channel, count ->
                println("Subscribed to $channel. Total count is $count")
            }
            onPSubscribed = { pattern, count ->
                println("PSubscribed to $pattern. Total count is $count")
            }
        }
        addListener(listener)
        // ....
        removeListener(listener)
    }

    client.async {
        set("key", "value")
    }

    val test = client.sync {
        get("key")
    }

    // 如果 Pool是Async 应该采用异步的方式操作
    client.asyncPool?.let { pool ->
        pool.acquire().thenApply {
            it.sync().get("key")
        }
    }

    client.sync {
        get("key")
    }

    client.reactive {
        multi().publishOn(Schedulers.boundedElastic()).doOnSuccess {
            set("key", "value").subscribe()
        }.flatMap {
            get("key")
        }.subscribe()
    }

    // 面对更复杂的需求可以使用字符串手动写连接
    val moreClient = buildClient {
        this input "redis://localhost:6379"
    }.link<SingleClient> {
        // do something
    }

    val masterSlaveClient = buildClient {
        "localhost" link 6379 password "password" ssl true
    }.link<MasterSlaveClient> {
        // 添加从节点
        connect(
            getURI { "localhost" link 6379 },
            getURI { "localhost" link 6380 },
            getURI { "localhost" link 6381 }
        )
    }

    masterSlaveClient.async {
        set("key", "value")
    }

    val cluster = buildClient {
        "localhost" link 6379 password "password" ssl true
    }.link<ClusterClient> {
        connect(
            "sub1" to getURI { "localhost" link 6379 },
            "sub2" to getURI { "localhost" link 6380 },
            "sub3" to getURI { "localhost" link 6381 }
        )
        enableTopologyRefresh(Duration.ofMinutes(50L))
    }

    cluster.sync {
        set("key", "value")
    }

    cluster.asyncPool?.let { pool ->
        pool.thenAccept { poolA ->
            poolA.acquire().thenApply {
                it.sync().get("key")
            }
        }
    }

}

fun buildClient(action: URIBuilder.() -> Unit): LettuceClientConverter {
    return URIBuilder().apply(action).build()
}

fun getURI(action: URIBuilder.() -> Unit): RedisURI {
    return URIBuilder().apply(action).getURI()
}

class URIBuilder(id: String = "") {

    companion object {
        fun formConfig(config: ConfigurationSection): LettuceClientConverter {
            val builder = URIBuilder()
            config.getString("host")?.let { builder.host(it) } ?: error("Host not found.")
            config.getInt("port").let { builder.port(it) }
            config.getString("password")?.let { builder.password(it) }
            config.getBoolean("ssl").let { builder.ssl(it) }
            config.getString("timeout")?.let { builder.timeout(Duration.parse(it)) }
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

}
