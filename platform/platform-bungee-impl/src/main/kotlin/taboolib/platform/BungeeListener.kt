package taboolib.platform

import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventBus
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.service.PlatformListener
import taboolib.common.util.unsafeLazy
import java.lang.reflect.Method

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@PlatformSide(Platform.BUNGEE)
class BungeeListener : PlatformListener {

    val plugin by unsafeLazy { BungeePlugin.getInstance() }

    val eventBus by unsafeLazy {
        BungeeCord.getInstance().pluginManager.getProperty<EventBus>("eventBus")!!
    }

    val byListenerAndPriority by unsafeLazy {
        eventBus.getProperty<MutableMap<Class<*>, MutableMap<Byte, MutableMap<Any, Array<Method>>>>>("byListenerAndPriority")!!
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, level: Int, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = BungeeListener(event, level) { func(it as T) }
        val priority = byListenerAndPriority.computeIfAbsent(event) { HashMap() }
        val listenerMap = priority.computeIfAbsent(level.toByte()) { HashMap() }
        listenerMap[listener] = arrayOf(BungeeListener.handleMethod)
        eventBus.invokeMethod<Void>("bakeHandlers", event)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        val listener = proxyListener as BungeeListener
        val priority = byListenerAndPriority[listener.cls] ?: return
        val listenerMap = priority[listener.level.toByte()] ?: return
        listenerMap.remove(listener)
        eventBus.invokeMethod<Void>("bakeHandlers", listener.cls)
    }

    class BungeeListener(val cls: Class<*>, val level: Int, val consumer: (Any) -> Unit) : Listener, ProxyListener {

        fun handle(event: Any) {
            if (cls.isAssignableFrom(event.javaClass)) {
                consumer(event)
            }
        }

        companion object {

            val handleMethod: Method = BungeeListener::class.java.getDeclaredMethod("handle", Any::class.java).also {
                it.isAccessible = true
            }
        }
    }
}