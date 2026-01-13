package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Awake

@Inject
@RuntimeDependencies(
    RuntimeDependency(
        "!io.lettuce:lettuce-core:7.2.1.RELEASE",
        test = "!io.lettuce.core.RedisURI",
        relocate = ["!io.netty", "!io.netty_4_2_5_final",
            "!org.apache.commons.pool2", "!org.apache.commons.pool2_2_12_1",
            "!reactor", "!reactor_3_6_6",
            "!org.reactivestreams", "!org.reactivestreams_1_0_4",
            "!org.slf4j", "!org.slf4j_1_7_36",
            "!redis.clients.authentication", "!redis.clients.authentication_0_1_1_beta2"],
        transitive = false
    ),
    RuntimeDependency(
        "!org.apache.commons:commons-pool2:2.12.1",
        test = "!org.apache.commons.pool2_2_12_1.BaseObject",
        relocate = ["!org.apache.commons.pool2", "!org.apache.commons.pool2_2_12_1"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-common:4.2.5.Final",
        test = "!io.netty_4_2_5_final.util.AbstractConstant",
        relocate = ["!io.netty", "!io.netty_4_2_5_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-handler:4.2.5.Final",
        test = "!io.netty_4_2_5_final.handler.ssl.SslHandler",
        relocate = ["!io.netty", "!io.netty_4_2_5_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-resolver-dns:4.2.5.Final",
        test = "!io.netty_4_2_5_final.resolver.dns.DnsNameResolver",
        relocate = ["!io.netty", "!io.netty_4_2_5_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-transport:4.2.5.Final",
        test = "!io.netty_4_2_5_final.channel.Channel",
        relocate = ["!io.netty", "!io.netty_4_2_5_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-buffer:4.2.5.Final",
        test = "!io.netty_4_2_5_final.buffer.ByteBuf",
        relocate = ["!io.netty", "!io.netty_4_2_5_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-codec-base:4.2.5.Final",
        test = "!io.netty_4_2_5_final.handler.codec.ByteToMessageDecoder",
        relocate = ["!io.netty", "!io.netty_4_2_5_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-resolver:4.2.5.Final",
        test = "!io.netty_4_2_5_final.resolver.AddressResolver",
        relocate = ["!io.netty", "!io.netty_4_2_5_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-transport-native-unix-common:4.2.5.Final",
        test = "!io.netty_4_2_5_final.channel.unix.UnixChannel",
        relocate = ["!io.netty", "!io.netty_4_2_5_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-codec-dns:4.2.5.Final",
        test = "!io.netty_4_2_5_final.handler.codec.dns.DnsRecord",
        relocate = ["!io.netty", "!io.netty_4_2_5_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!org.reactivestreams:reactive-streams:1.0.4",
        test = "!org.reactivestreams_1_0_4.Publisher",
        relocate = ["!org.reactivestreams", "!org.reactivestreams_1_0_4"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!org.slf4j:slf4j-api:1.7.36",
        test = "!org.slf4j_1_7_36.Logger",
        relocate = ["!org.slf4j", "!org.slf4j_1_7_36"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.projectreactor:reactor-core:3.6.6",
        test = "!reactor_3_6_6.core.publisher.Flux",
        relocate = ["!reactor", "!reactor_3_6_6",
            "!org.reactivestreams", "!org.reactivestreams_1_0_4"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!redis.clients.authentication:redis-authx-core:0.1.1-beta2",
        test = "!redis.clients.authentication_0_1_1_beta2.core.TokenManager",
        relocate = ["!redis.clients.authentication", "!redis.clients.authentication_0_1_1_beta2"],
        transitive = false
    )
)
object LettuceRedis {

    internal val clients = mutableListOf<LettuceRedisClient>()
    internal val clusterClients = mutableListOf<LettuceClusterRedisClient>()

    @Awake(LifeCycle.DISABLE)
    internal fun stop() {
        clients.forEach {
            it.stop()
        }
        clusterClients.forEach {
            it.stop()
        }
    }
}