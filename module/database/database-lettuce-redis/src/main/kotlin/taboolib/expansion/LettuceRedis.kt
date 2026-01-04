package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Awake

@Inject
@RuntimeDependencies(
    RuntimeDependency(
        "!io.lettuce:lettuce-core:6.6.0.RELEASE",
        test = "!io.lettuce.core.RedisURI",
        relocate = ["!io.netty", "!io.netty_4_1_118_final",
            "!org.apache.commons.pool2", "!org.apache.commons.pool2_2_12_1",
            "!reactor", "!reactor_3_6_6",
            "!org.reactivestreams", "!org.reactivestreams_1_0_4"],
        transitive = false
    ),
    RuntimeDependency(
        "!org.reactivestreams:reactive-streams:1.0.4",
        test = "!org.reactivestreams_1_0_4.Publisher",
        relocate = ["!org.reactivestreams", "!org.reactivestreams_1_0_4"],
        transitive = false
    ),
    RuntimeDependency(
        "!io.projectreactor:reactor-core:3.6.6",
        test = "!reactor_3_6_6.core.CorePublisher",
        relocate = ["!reactor", "!reactor_3_6_6", "!org.reactivestreams", "!org.reactivestreams_1_0_4"],
        transitive = false
    ),
    RuntimeDependency(
        "!org.apache.commons:commons-pool2:2.12.1",
        test = "!org.apache.commons.pool2_2_12_1.BaseObject",
        relocate = ["!org.apache.commons.pool2", "!org.apache.commons.pool2_2_12_1"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-common:4.1.118.Final",
        test = "!io.netty_4_1_118_final.util.AbstractConstant",
        relocate = ["!io.netty", "!io.netty_4_1_118_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-buffer:4.1.118.Final",
        test = "!io.netty_4_1_118_final.buffer.AbstractByteBuf",
        relocate = ["!io.netty", "!io.netty_4_1_118_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-codec:4.1.118.Final",
        test = "!io.netty_4_1_118_final.handler.codec.AsciiHeadersEncoder",
        relocate = ["!io.netty", "!io.netty_4_1_118_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-handler:4.1.118.Final",
        test = "!io.netty_4_1_118_final.handler.address.ResolveAddressHandler",
        relocate = ["!io.netty", "!io.netty_4_1_118_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-resolver:4.1.118.Final",
        test = "!io.netty_4_1_118_final.resolver.AbstractAddressResolver",
        relocate = ["!io.netty", "!io.netty_4_1_118_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-transport:4.1.118.Final",
        test = "!io.netty_4_1_118_final.bootstrap.Bootstrap",
        relocate = ["!io.netty", "!io.netty_4_1_118_final"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!io.netty:netty-transport-native-unix-common:4.1.118.Final",
        test = "!io.netty_4_1_118_final.channel.unix.Buffer",
        relocate = ["!io.netty", "!io.netty_4_1_118_final"],
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