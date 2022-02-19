@file:Isolated
package taboolib.common.platform.function

import taboolib.common.Isolated
import taboolib.common.LifeCycle
import taboolib.common.TabooLibCommon
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory

inline val runningPlatform: Platform
    get() = TabooLibCommon.getRunningPlatform()

fun disablePlugin() {
    TabooLibCommon.setStopped(true)
}

fun postpone(lifeCycle: LifeCycle = LifeCycle.ENABLE, runnable: Runnable) {
    TabooLibCommon.postpone(lifeCycle, runnable)
}

inline fun <reified T> implementations(): T {
    return PlatformFactory.getAPI()
}