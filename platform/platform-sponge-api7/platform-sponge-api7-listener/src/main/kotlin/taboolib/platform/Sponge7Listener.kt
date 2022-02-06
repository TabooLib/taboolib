package taboolib.platform

import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Event
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
@PlatformSide([Platform.SPONGE_API_7])
class Sponge7Listener : PlatformListener {

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("Unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, order: EventOrder, beforeModifications: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = Sponge7RegisteredListener<Event>(event) { func(it as T) }
        val eventClass = event.getEventClass()
        Sponge.getEventManager().registerListener(this, eventClass as Class<Event>, Order.values()[order.ordinal], beforeModifications, listener)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        Sponge.getEventManager().unregisterListeners(proxyListener)
    }

    class Sponge7Listener
}