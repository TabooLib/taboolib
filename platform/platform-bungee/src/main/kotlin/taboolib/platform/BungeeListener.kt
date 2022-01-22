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
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArraySet

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

    val plugin by lazy { BungeePlugin.getInstance() }

    val eventBus by lazy {
        BungeeCord.getInstance().pluginManager.getProperty<EventBus>("eventBus")!!
    }

    val byListenerAndPriority by lazy {
        eventBus.getProperty<MutableMap<Class<*>, MutableMap<Byte, MutableMap<Any, Array<Method>>>>>("byListenerAndPriority")!!
    }

    val byEventBaked by lazy {
        eventBus.getProperty<MutableMap<Class<*>, Array<EventHandlerMethod>>>("byEventBaked")!!
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, level: Int, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = BungeeListener(event, level) { func(it as T) }
        var array = emptyArray<EventHandlerMethod>()
        val eventClass = event.getUsableEvent()
        byListenerAndPriority.computeIfAbsent(eventClass) { HashMap() }.run {
            computeIfAbsent(level.toByte()) { HashMap() }.run {
                put(listener, arrayOf(BungeeListener.method))
                forEach { (listener, methods) -> methods.forEach { array += EventHandlerMethod(listener, it) } }
            }
        }
        byEventBaked[eventClass] = array
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        val listener = proxyListener as BungeeListener
        var array = emptyArray<EventHandlerMethod>()
        val eventClass = listener.clazz.getUsableEvent()
        byListenerAndPriority[eventClass]?.run {
            get(listener.level.toByte())?.run {
                remove(listener)
                forEach { (listener, methods) -> methods.forEach { array += EventHandlerMethod(listener, it) } }
            }
        }
        byEventBaked[eventClass] = array
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