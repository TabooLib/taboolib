package taboolib.platform

import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Event
import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.EventListenerRegistration
import org.spongepowered.api.event.Order
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventOrder
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.getUsableEvent
import taboolib.common.platform.function.isPlatformEvent
import taboolib.common.platform.service.PlatformListener
import org.tabooproject.reflex.Reflex.Companion.getProperty

/**
 * TabooLib
 * taboolib.platform.SpongeAdapter
 *
 * @author tr
 * @since 2021/6/21 17:02
 */
@Awake
@PlatformSide([Platform.SPONGE_API_9])
class Sponge9Listener : PlatformListener {

    val plugin by unsafeLazy {
        Sponge9Plugin.getInstance()
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, order: EventOrder, beforeModifications: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = Sponge9Listener<Event>(event) { func(it as T) }
        val eventClass = event.getUsableEvent()
        Sponge.eventManager().registerListener(EventListenerRegistration.builder(eventClass as Class<Event>).apply {
            order(Order.values()[order.ordinal])
            plugin(plugin.pluginContainer)
            beforeModifications(beforeModifications)
            listener(listener)
        }.build())
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        Sponge.eventManager().unregisterListeners(proxyListener)
    }

    class Sponge9Listener<T : Event>(private val clazz: Class<*>, val consumer: (Any) -> Unit) : EventListener<T>, ProxyListener {

        override fun handle(event: T) {
            val origin = if (event::class.java.isPlatformEvent) event.getProperty<Any>("proxyEvent") ?: event else event
            if (clazz.isAssignableFrom(origin.javaClass)) {
                consumer(origin)
            }
        }
    }
}