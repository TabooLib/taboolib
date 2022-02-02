package taboolib.common5

import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency

@RuntimeDependencies(
    RuntimeDependency("!com.google.guava:guava:21.0", test = "!com.google.common.base.Optional"),
    RuntimeDependency("!com.google.code.gson:gson:2.8.7", test = "!com.google.gson.JsonElement")
)
object Common5