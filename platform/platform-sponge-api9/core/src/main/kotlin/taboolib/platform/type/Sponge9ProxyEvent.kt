package taboolib.platform.type

import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Cancellable
import org.spongepowered.api.event.Cause
import org.spongepowered.api.event.EventContext
import org.spongepowered.api.event.EventContextKeys
import org.spongepowered.api.event.impl.AbstractEvent
import taboolib.common.platform.event.ProxyEvent
import taboolib.platform.Sponge9Plugin

open class Sponge9ProxyEvent(val proxyEvent: ProxyEvent? = null) : AbstractEvent(), Cancellable {

    private var isCancelled = false

    open val allowCancelled: Boolean
        get() = true

    val eventContext: EventContext = EventContext.builder().add(EventContextKeys.PLUGIN, Sponge9Plugin.getInstance().pluginContainer).build()
    val eventCause: Cause = Cause.of(eventContext, Sponge9Plugin.getInstance().pluginContainer)

    override fun isCancelled(): Boolean {
        return proxyEvent?.isCancelled ?: isCancelled
    }

    override fun setCancelled(value: Boolean) {
        if (proxyEvent != null) {
            if (proxyEvent.allowCancelled) {
                proxyEvent.isCancelled = value
            } else {
                error("Unsupported")
            }
        } else if (allowCancelled) {
            isCancelled = value
        } else {
            error("unsupported")
        }
    }

    override fun cause(): Cause {
        return eventCause
    }

    fun call(): Boolean {
        Sponge.eventManager().post(this)
        return !isCancelled
    }
}