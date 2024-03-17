package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency

@RuntimeDependencies(
    RuntimeDependency(
        "!io.lettuce:lettuce-core:6.3.2.RELEASE",
        test = "!io.lettuce.core_6_3_2.RedisURI",
        relocate = ["!io.lettuce.core", "!io.lettuce.core_6_3_2"],
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



}