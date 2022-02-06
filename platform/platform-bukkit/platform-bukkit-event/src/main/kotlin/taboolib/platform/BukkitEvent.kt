package taboolib.platform

import org.bukkit.event.Event
import taboolib.internal.Internal
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent
import taboolib.platform.type.BukkitProxyEvent

/**
 * TabooLib
 * taboolib.platform.BukkitAdapter
 *
 * @author sky
 * @since 2021/6/17 12:22 上午
 */
@Internal
@Awake
@PlatformSide([Platform.BUKKIT])
class BukkitEvent : PlatformEvent {

    override fun callEvent(proxyEvent: ProxyEvent) {
        fireEvent(BukkitProxyEvent(proxyEvent))
        proxyEvent.postCall()
    }

    fun fireEvent(event: Event) {
        event.handlers.registeredListeners.forEach {
            if (it.plugin.isEnabled) {
                try {
                    it.callEvent(event)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}