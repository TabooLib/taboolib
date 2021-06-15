package taboolib.module.dependency

import taboolib.common.platform.PlatformAPI
import taboolib.module.dependency.env.AsmEnv

@PlatformAPI
object TDependency {

    init {
        AsmEnv.init()
    }
}