package taboolib.expansion

import io.lettuce.core.RedisURI
import taboolib.common.Inject
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.expansion.client.PoolType
import taboolib.expansion.client.impl.ClusterClient
import taboolib.expansion.client.impl.MasterSlaveClient
import taboolib.expansion.client.impl.SingleClient
import taboolib.library.configuration.ConfigurationSection

@RuntimeDependencies(
    RuntimeDependency(
        "!io.lettuce:lettuce-core:6.3.2.RELEASE",
        test = "!io.lettuce.core.RedisURI",
        relocate = ["!io.netty.resolver.dns", "!io.netty.resolver.dns_4_1_107_final"],
        transitive = false
    ),
    RuntimeDependency(
        "!io.projectreactor:reactor-core:3.6.4",
        test = "!reactor.util.context.Context",
    ),
    RuntimeDependency(
        "!org.reactivestreams:reactive-streams:1.0.4",
        test = "!org.reactivestreams.Publisher"
    ),
    // 解决低版本NettyDNS冲突
    RuntimeDependency(
        value = "!io.netty:netty-resolver-dns:4.1.107.Final",
        test = "!io.netty.resolver.dns_4_1_107_final.DnsNameResolverBuilder",
        transitive = false,
        relocate = ["!io.netty.resolver.dns", "!io.netty.resolver.dns_4_1_107_final"]
    ),
    RuntimeDependency(
        "!org.apache.commons:commons-pool2:2.11.1",
        test = "!org.apache.commons.pool2.ObjectPool",
        transitive = false
    ),
)
@Inject
object LettuceRedis {

    // 快速创建单机客户端
    fun fastCreateSingleClient(config: ConfigurationSection): SingleClient {
        val poolType = config.getString("pool.type") ?: "NONE"
        return URIBuilder.formConfig(config).link<SingleClient>(PoolType.valueOf(poolType))
    }

    // 主从
    fun fastCreateMasterSlaveClient(config: ConfigurationSection): MasterSlaveClient {
        val poolType = config.getString("pool.type") ?: "NONE"
        val type = PoolType.valueOf(poolType)
        val client = URIBuilder.formConfig(config).link<MasterSlaveClient>(type)

        val uriList = mutableListOf<RedisURI>()
        config.getStringList("master_slave.slave").forEach {
            uriList.add(buildURI { this input it })
        }
        client.connect(*uriList.toTypedArray())
        return client
    }

    // 集群
    fun fastCreateClusterClient(config: ConfigurationSection): ClusterClient {
        val poolType = config.getString("pool.type") ?: "NONE"
        val type = PoolType.valueOf(poolType)
        val client = URIBuilder.formConfig(config).link<ClusterClient>(type)

        val uriList = mutableListOf<Pair<String, RedisURI>>()
        config.getStringList("cluster.nodes").forEach {
            val args = it.split(" to ")
            uriList.add(args[0] to buildURI { this input args[1] })
        }
        client.connect(*uriList.toTypedArray())
        return client
    }

//    fun test() {
//        val client = buildClient {
//            "localhost" link 6379 password "password" ssl true
//            // or
//            host("localhost")
//            port(6379)
//            password("password")
//            ssl(true)
//        }.link<SingleClient>(pool = PoolType.SYNC) {
//            // do something
//
//            val listener = pubSubListener {
//                onSubscribed = { channel, count ->
//                    println("Subscribed to $channel. Total count is $count")
//                }
//                onPSubscribed = { pattern, count ->
//                    println("PSubscribed to $pattern. Total count is $count")
//                }
//            }
//            addListener(listener)
//            // ....
//            removeListener(listener)
//        }
//
//        client.async {
//            set("key", "value")
//        }
//
//        val test = client.sync {
//            get("key")
//        }
//
//        // 如果 Pool是Async 应该采用异步的方式操作
//        client.asyncPool?.let { pool ->
//            pool.acquire().thenApply {
//                it.sync().get("key")
//            }
//        }
//
//        client.sync {
//            get("key")
//        }
//
//        client.reactive {
//            multi().publishOn(Schedulers.boundedElastic()).doOnSuccess {
//                set("key", "value").subscribe()
//            }.flatMap {
//                get("key")
//            }.subscribe()
//        }
//
//        // 面对更复杂的需求可以使用字符串手动写连接
//        val moreClient = buildClient {
//            this input "redis://localhost:6379"
//        }.link<SingleClient> {
//            // do something
//        }
//
//        val masterSlaveClient = buildClient {
//            "localhost" link 6379 password "password" ssl true
//        }.link<MasterSlaveClient> {
//            // 添加从节点
//            connect(
//                buildURI { "localhost" link 6379 },
//                buildURI { "localhost" link 6380 },
//                buildURI { "localhost" link 6381 }
//            )
//        }
//
//        masterSlaveClient.async {
//            set("key", "value")
//        }
//
//        val cluster = buildClient {
//            "localhost" link 6379 password "password" ssl true
//        }.link<ClusterClient> {
//            connect(
//                "sub1" to buildURI { "localhost" link 6379 },
//                "sub2" to buildURI { "localhost" link 6380 },
//                "sub3" to buildURI { "localhost" link 6381 }
//            )
//            enableTopologyRefresh(Duration.ofMinutes(50L))
//        }
//
//        cluster.sync {
//            set("key", "value")
//        }
//
//        cluster.asyncPool?.let { pool ->
//            pool.thenAccept { poolA ->
//                poolA.acquire().thenApply {
//                    it.sync().get("key")
//                }
//            }
//        }
//
//    }

}