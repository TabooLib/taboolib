package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency

@RuntimeDependencies(
    RuntimeDependency(
        "!io.lettuce:lettuce-core:6.3.2.RELEASE",
        test = "!io.lettuce.core.RedisURI",
        transitive = false
    ),
    RuntimeDependency(
        "!io.projectreactor:reactor-core:3.6.4",
    ),
    RuntimeDependency(
        "!org.reactivestreams:reactive-streams:1.0.4",
        test = "!org.reactivestreams.Publisher"
    ),
    //implementation("io.netty:netty-resolver-dns:4.1.107.Final")
    RuntimeDependency(
        "!io.netty:netty-resolver-dns:4.1.107.Final",
        test = "!io.netty.resolver.dns.DnsCnameCache",
        transitive = false
    ),
    RuntimeDependency(
        "!org.apache.commons:commons-pool2:2.11.1",
        test = "!org.apache.commons.pool2.ObjectPool",
        transitive = false
    ),
)
@Inject
object LettuceRedis {

    fun test(){
    }

}