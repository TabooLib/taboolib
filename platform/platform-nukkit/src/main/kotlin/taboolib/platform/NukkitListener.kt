package taboolib.platform

import cn.nukkit.Server
import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList
import cn.nukkit.event.Listener
import cn.nukkit.plugin.EventExecutor
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.getUsableEvent
import taboolib.common.platform.function.isPlatformEvent
import taboolib.common.platform.service.PlatformListener
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.util.unsafeLazy
import java.util.concurrent.CopyOnWriteArraySet

/**
 * TabooLib
 * taboolib.platform.NukkitAdapter
 *
 * @author CziSKY
 * @since 2021/6/20 0:46
 */
@Awake
@PlatformSide([Platform.NUKKIT])
class NukkitListener : PlatformListener {

    private val plugin by unsafeLazy { NukkitPlugin.getInstance() }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = NukkitListener(event as Class<Event>) { func(it as T) }
        val eventClass = event.getUsableEvent()
        Server.getInstance().pluginManager.registerEvent(eventClass as Class<Event>, listener, priority.toNukkit(), listener, plugin, ignoreCancelled)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        HandlerList.unregisterAll(proxyListener as Listener)
    }

    fun EventPriority.toNukkit() = when (this) {
        EventPriority.LOWEST -> cn.nukkit.event.EventPriority.LOWEST
        EventPriority.LOW -> cn.nukkit.event.EventPriority.LOW
        EventPriority.NORMAL -> cn.nukkit.event.EventPriority.NORMAL
        EventPriority.HIGH -> cn.nukkit.event.EventPriority.HIGH
        EventPriority.HIGHEST -> cn.nukkit.event.EventPriority.HIGHEST
        EventPriority.MONITOR -> cn.nukkit.event.EventPriority.MONITOR
    }

    class NukkitListener(private val clazz: Class<*>, val consumer: (Any) -> Unit) : Listener, EventExecutor, ProxyListener {

        val isVanillaEvent = Event::class.java.isAssignableFrom(clazz)
        val ignored = CopyOnWriteArraySet<Class<*>>()

        override fun execute(listener: Listener, event: Event) {
            if (ignored.contains(event.javaClass)) {
                return
            }
            var origin: Any = event
            if (!isVanillaEvent) {
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
}