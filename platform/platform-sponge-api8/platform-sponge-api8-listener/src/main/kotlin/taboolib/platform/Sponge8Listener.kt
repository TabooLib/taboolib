package taboolib.platform

import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Event
import org.spongepowered.api.event.EventListenerRegistration
import org.spongepowered.api.event.Order
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventOrder
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.service.PlatformListener
import taboolib.common.platform.function.getEventClass
import taboolib.internal.Internal

/**
 * TabooLib
 * taboolib.platform.SpongeAdapter
 *
 * @author tr
 * @since 2021/6/21 17:02
 */
@Internal
@Awake
@PlatformSide([Platform.SPONGE_API_8])
class Sponge8Listener : PlatformListener {

    val plugin by lazy {
        Sponge8Plugin.getInstance()
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("Unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, order: EventOrder, beforeModifications: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = Sponge8RegisteredListener<Event>(event) { func(it as T) }
        val eventClass = event.getEventClass()
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
}