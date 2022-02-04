@file:Isolated

package taboolib.common.platform.function

import taboolib.common.Isolated
import taboolib.common.TabooLib
import taboolib.internal.SimpleMonitor

fun disablePlugin() {
    val monitor = TabooLib.monitor()
    if (monitor is SimpleMonitor) {
        monitor.isShutdown = true
    } else {
        throw NotImplementedError()
    }
}