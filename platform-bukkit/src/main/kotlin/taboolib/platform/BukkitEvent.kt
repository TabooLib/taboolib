package taboolib.platform

import org.bukkit.Bukkit
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
        Bukkit.getPluginManager().callEvent(bukkitEvent)
        bukkitEvent.proxyEvent?.postCall()
    }
}