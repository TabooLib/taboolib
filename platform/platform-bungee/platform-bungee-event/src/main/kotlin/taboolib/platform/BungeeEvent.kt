package taboolib.platform

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent
import taboolib.internal.Internal
import taboolib.platform.type.BungeeProxyEvent

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Internal
@Awake
@PlatformSide([Platform.BUNGEE])
class BungeeEvent : PlatformEvent {

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = BungeeProxyEvent(proxyEvent)
        BungeePlugin.getInstance().proxy.pluginManager.callEvent(event)
        event.proxyEvent?.postCall()
    }
}