package taboolib.platform

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent
import taboolib.common.util.unsafeLazy
import taboolib.platform.type.BungeeProxyEvent

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@PlatformSide([Platform.BUNGEE])
class BungeeEvent : PlatformEvent {

    val plugin by unsafeLazy { BungeePlugin.getInstance() }

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = BungeeProxyEvent(proxyEvent)
        plugin.proxy.pluginManager.callEvent(event)
        event.proxyEvent?.postCall()
    }
}