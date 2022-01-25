@file:Isolated
package taboolib.common.platform.function

import taboolib.common.Isolated
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory

inline val runningPlatform: Platform
    get() = TabooLib.runningPlatform()

fun disablePlugin() {
    TabooLib.setStopped(true)
}

fun postpone(lifeCycle: LifeCycle = LifeCycle.ENABLE, runnable: Runnable) {
    TabooLib.postpone(lifeCycle, runnable)
}