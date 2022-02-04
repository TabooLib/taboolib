package taboolib.platform

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.plugin.EventExecutor
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.isPlatformEvent
import taboolib.common.platform.service.PlatformListener
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.internal.Internal
import taboolib.common.platform.function.getEventClass
import java.util.concurrent.CopyOnWriteArraySet

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
        val listener = Listener(event) { func(it as T) }
        Bukkit.getPluginManager().registerEvent(event.getEventClass() as Class<Event>, listener, priority.toBukkit(), listener, plugin, ignoreCancelled)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        HandlerList.unregisterAll(proxyListener as org.bukkit.event.Listener)
    }

    @Internal
    class Listener(private val clazz: Class<*>, val consumer: (Any) -> Unit) : org.bukkit.event.Listener, EventExecutor, ProxyListener {

        val isProxyEvent = !Event::class.java.isAssignableFrom(clazz)
        val ignored = CopyOnWriteArraySet<Class<*>>()

        override fun execute(listener: org.bukkit.event.Listener, event: Event) {
            if (ignored.contains(event.javaClass)) {
                return
            }
            var origin: Any = event
            if (isProxyEvent) {
                if (event.javaClass.isPlatformEvent) {
                    origin = event.getProperty("proxyEvent") ?: origin
                } else {
                    ignored += event.javaClass
                    return
                }
            }
            if (clazz.isAssignableFrom(origin.javaClass)) {
                consumer(origin)
            } else {
                ignored += event.javaClass
            }
        }
    }

    fun EventPriority.toBukkit() = when (this) {
        EventPriority.LOWEST -> org.bukkit.event.EventPriority.LOWEST
        EventPriority.LOW -> org.bukkit.event.EventPriority.LOW
        EventPriority.NORMAL -> org.bukkit.event.EventPriority.NORMAL
        EventPriority.HIGH -> org.bukkit.event.EventPriority.HIGH
        EventPriority.HIGHEST -> org.bukkit.event.EventPriority.HIGHEST
        EventPriority.MONITOR -> org.bukkit.event.EventPriority.MONITOR
    }
}