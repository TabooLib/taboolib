package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency

@RuntimeDependencies(
    RuntimeDependency(
        "!io.lettuce:lettuce-core:6.3.2.RELEASE",
        test = "!io.lettuce.core.RedisURI",
        relocate = ["!io.netty.resolver.dns","!io.netty.resolver.dns_4_1_107_final"],
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
        relocate = ["!io.netty.resolver.dns","!io.netty.resolver.dns_4_1_107_final"]
    ),
    RuntimeDependency(
        "!org.apache.commons:commons-pool2:2.11.1",
        test = "!org.apache.commons.pool2.ObjectPool",
        transitive = false
    ),
)
@Inject
object LettuceRedis {

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
//                getURI { "localhost" link 6379 },
//                getURI { "localhost" link 6380 },
//                getURI { "localhost" link 6381 }
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
//                "sub1" to getURI { "localhost" link 6379 },
//                "sub2" to getURI { "localhost" link 6380 },
//                "sub3" to getURI { "localhost" link 6381 }
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