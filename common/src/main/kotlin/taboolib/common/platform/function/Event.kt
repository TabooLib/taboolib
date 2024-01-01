@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common.platform.function

import taboolib.common.Isolated
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent

inline fun callEvent(proxyEvent: ProxyEvent) {
    PlatformFactory.getService<PlatformEvent>().callEvent(proxyEvent)
}