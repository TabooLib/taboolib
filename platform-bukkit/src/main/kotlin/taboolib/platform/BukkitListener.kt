package taboolib.platform

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.getUsableEvent
import taboolib.common.platform.function.isPlatformEvent
import taboolib.common.platform.service.PlatformListener
import taboolib.common.reflect.Reflex.Companion.getProperty

/**
 * TabooLib
 * taboolib.platform.BukkitAdapter
 *
 * @author sky
 * @since 2021/6/17 12:22 上午
 */
@Awake
@PlatformSide([Platform.BUKKIT])
class BukkitListener : PlatformListener {

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = BukkitListener(event as Class<Event>) { func(it as T) }
        Bukkit.getPluginManager().registerEvent(event.getUsableEvent() as Class<Event>, listener, priority.toBukkit(), listener, plugin, ignoreCancelled)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        HandlerList.unregisterAll(proxyListener as Listener)
    }

    fun EventPriority.toBukkit() = when (this) {
        EventPriority.LOWEST -> org.bukkit.event.EventPriority.LOWEST
        EventPriority.LOW -> org.bukkit.event.EventPriority.LOW
        EventPriority.NORMAL -> org.bukkit.event.EventPriority.NORMAL
        EventPriority.HIGH -> org.bukkit.event.EventPriority.HIGH
        EventPriority.HIGHEST -> org.bukkit.event.EventPriority.HIGHEST
        EventPriority.MONITOR -> org.bukkit.event.EventPriority.MONITOR
    }

    class BukkitListener(val clazz: Class<*>, val consumer: (Any) -> Unit) : Listener, EventExecutor, ProxyListener {

        override fun execute(listener: Listener, event: Event) {
            val origin = if (event::class.java.isPlatformEvent) event.getProperty<Any>("proxyEvent")!! else event
            if (origin.javaClass == clazz) {
                consumer(origin)
            }
        }
    }
}