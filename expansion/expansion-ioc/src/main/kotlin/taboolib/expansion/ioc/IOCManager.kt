package taboolib.expansion.ioc

import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency

@RuntimeDependencies(
    RuntimeDependency(value = "!com.google.code.gson:gson:2.8.7", test = "!com.google.gson.JsonElement")
)
object IOCManager {


}