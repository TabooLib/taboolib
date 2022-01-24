package taboolib.platform

import com.google.common.base.Enums
import org.bukkit.event.Event
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
@Awake
@PlatformSide([Platform.BUKKIT])
class BukkitEvent : PlatformEvent {

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    override fun callEvent(proxyEvent: ProxyEvent) {
        val bukkitEvent = BukkitProxyEvent(proxyEvent)
        fireEvent(bukkitEvent)
        bukkitEvent.proxyEvent?.postCall()
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