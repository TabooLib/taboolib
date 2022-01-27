@file:Isolated

package taboolib.common.platform.function

import taboolib.common.Isolated
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.internal.SimpleMonitor

fun disablePlugin() {
    val monitor = TabooLib.monitor()
    if (monitor is SimpleMonitor) {
        monitor.isShutdown = true
    } else {
        throw NotImplementedError()
    }
}

fun postpone(lifeCycle: LifeCycle = LifeCycle.ENABLE, runnable: Runnable) {
    TabooLib.booster().join(lifeCycle, runnable)
}