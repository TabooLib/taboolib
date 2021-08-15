package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent

fun callEvent(proxyEvent: ProxyEvent) {
    PlatformFactory.getService<PlatformEvent>().callEvent(proxyEvent)
}