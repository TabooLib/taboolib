package taboolib.module.dependency

import taboolib.common.platform.PlatformInstance
import taboolib.module.dependency.env.AsmEnv

@PlatformInstance
object TDependency {

    init {
        AsmEnv.init()
    }
}