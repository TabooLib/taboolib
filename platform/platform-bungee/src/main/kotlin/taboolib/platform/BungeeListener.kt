package taboolib.platform

import net.md_5.bungee.BungeeCord
import net.md_5.bungee.event.EventBus
import net.md_5.bungee.event.EventHandlerMethod
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.getUsableEvent
import taboolib.common.platform.function.isPlatformEvent
import taboolib.common.platform.service.PlatformListener
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
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
@PlatformSide([Platform.BUNGEE])
class BungeeListener : PlatformListener {

    val plugin by unsafeLazy { BungeePlugin.getInstance() }

    val eventBus by unsafeLazy {
        BungeeCord.getInstance().pluginManager.getProperty<EventBus>("eventBus")!!
    }

    val byListenerAndPriority by unsafeLazy {
        eventBus.getProperty<MutableMap<Class<*>, MutableMap<Byte, MutableMap<Any, Array<Method>>>>>("byListenerAndPriority")!!
    }

    val byEventBaked by unsafeLazy {
        eventBus.getProperty<MutableMap<Class<*>, Array<EventHandlerMethod>>>("byEventBaked")!!
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, level: Int, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = BungeeListener(event, level) { func(it as T) }
        val eventClass = event.getUsableEvent()
        val priority = byListenerAndPriority.computeIfAbsent(eventClass) { HashMap() }
        val listenerMap = priority.computeIfAbsent(level.toByte()) { HashMap() }
        listenerMap[listener] = arrayOf(BungeeListener.method)
        eventBus.invokeMethod<Void>("bakeHandlers", eventClass)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        val listener = proxyListener as BungeeListener
        val eventClass = listener.clazz.getUsableEvent()
        val priority = byListenerAndPriority[eventClass] ?: return
        val listenerMap = priority[listener.level.toByte()] ?: return
        listenerMap.remove(listener)
        eventBus.invokeMethod<Void>("bakeHandlers", eventClass)
    }

    class BungeeListener(val clazz: Class<*>, val level: Int, val consumer: (Any) -> Unit) : ProxyListener {

        fun handle(event: Any) {
            val origin = if (event::class.java.isPlatformEvent) event.getProperty<Any>("proxyEvent") ?: event else event
            if (clazz.isAssignableFrom(origin.javaClass)) {
                consumer(origin)
            }
        }

        companion object {

            val method: Method = BungeeListener::class.java.getDeclaredMethod("handle", Any::class.java).also {
                it.isAccessible = true
            }
        }
    }
}