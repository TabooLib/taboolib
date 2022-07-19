package taboolib.platform

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent
import taboolib.platform.type.VelocityProxyEvent
import taboolib.common.util.unsafeLazy

/**
 * TabooLib
 * taboolib.platform.VelocityAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@PlatformSide([Platform.VELOCITY])
class VelocityEvent : PlatformEvent {

    val plugin by unsafeLazy { VelocityPlugin.getInstance() }

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = VelocityProxyEvent(proxyEvent)
        plugin.server.eventManager.fire(event)
        event.proxyEvent?.postCall()
    }
}