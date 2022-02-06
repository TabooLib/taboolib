package taboolib.platform

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.getEventClass
import taboolib.common.platform.service.PlatformListener
import taboolib.internal.Internal

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
class BukkitListener : PlatformListener {

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = BukkitRegisteredListener(event) { func(it as T) }
        Bukkit.getPluginManager().registerEvent(event.getEventClass() as Class<Event>, listener, bukkit(priority), listener, plugin, ignoreCancelled)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        HandlerList.unregisterAll(proxyListener as org.bukkit.event.Listener)
    }

    private fun bukkit(eventPriority: EventPriority) = when (eventPriority) {
        EventPriority.LOWEST -> org.bukkit.event.EventPriority.LOWEST
        EventPriority.LOW -> org.bukkit.event.EventPriority.LOW
        EventPriority.NORMAL -> org.bukkit.event.EventPriority.NORMAL
        EventPriority.HIGH -> org.bukkit.event.EventPriority.HIGH
        EventPriority.HIGHEST -> org.bukkit.event.EventPriority.HIGHEST
        EventPriority.MONITOR -> org.bukkit.event.EventPriority.MONITOR
    }
}