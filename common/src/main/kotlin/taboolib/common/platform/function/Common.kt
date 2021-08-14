package taboolib.common.platform.function

import taboolib.common.TabooLibCommon
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory

inline val runningPlatform: Platform
    get() = TabooLibCommon.getRunningPlatform()

fun disablePlugin() {
    TabooLibCommon.setStopped(true)
}

inline fun <reified T> implementations(): T {
    return PlatformFactory.getAPI()
}