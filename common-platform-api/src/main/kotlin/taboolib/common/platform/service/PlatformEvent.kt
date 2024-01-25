package taboolib.common.platform.service

import taboolib.common.platform.PlatformService
import taboolib.common.platform.event.ProxyEvent

/**
 * TabooLib
 * taboolib.common.platform.service.PlatformEvent
 *
 * @author sky
 * @since 2021/6/17 12:04 上午
 */
@PlatformService
interface PlatformEvent {

    fun callEvent(proxyEvent: ProxyEvent)
}