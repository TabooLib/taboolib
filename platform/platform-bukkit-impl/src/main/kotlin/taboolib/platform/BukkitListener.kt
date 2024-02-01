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
import taboolib.common.platform.service.PlatformListener
import java.util.concurrent.CopyOnWriteArraySet

/**
 * TabooLib
 * taboolib.platform.BukkitAdapter
 *
 * @author sky
 * @since 2021/6/17 12:22 上午
 */
@Awake
@PlatformSide(Platform.BUKKIT)
class BukkitListener : PlatformListener {

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        // 类型检查
        if (Event::class.java.isAssignableFrom(event)) {
            val listener = BukkitListener(event) { func(it as T) }
            val bukkitPriority = org.bukkit.event.EventPriority.values()[priority.ordinal]
            Bukkit.getPluginManager().registerEvent(event as Class<Event>, listener, bukkitPriority, listener, plugin, ignoreCancelled)
            return listener
        }
        error("unsupported event type: ${event.name}")
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        HandlerList.unregisterAll(proxyListener as Listener)
    }

    class BukkitListener(private val clazz: Class<*>, val consumer: (Any) -> Unit) : Listener, EventExecutor, ProxyListener {

        val ignored = CopyOnWriteArraySet<Class<*>>()

        override fun execute(listener: Listener, event: Event) {
            if (ignored.contains(event.javaClass)) {
                return
            }
            if (clazz.isAssignableFrom(event.javaClass)) {
                consumer(event)
            } else {
                ignored += event.javaClass
            }
        }
    }
}