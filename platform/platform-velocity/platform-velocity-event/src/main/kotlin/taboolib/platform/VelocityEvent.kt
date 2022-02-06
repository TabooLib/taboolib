package taboolib.platform

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent
import taboolib.internal.Internal
import taboolib.platform.type.VelocityProxyEvent

/**
 * TabooLib
 * taboolib.platform.VelocityAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Internal
@Awake
@PlatformSide([Platform.VELOCITY])
class VelocityEvent : PlatformEvent {

    override fun callEvent(proxyEvent: ProxyEvent) {
        VelocityPlugin.getInstance().server.eventManager.fire(VelocityProxyEvent(proxyEvent))
        proxyEvent.postCall()
    }
}