package taboolib.platform.type

import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Cancellable
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.EventContext
import org.spongepowered.api.event.cause.EventContextKeys
import org.spongepowered.api.event.impl.AbstractEvent
import taboolib.common.platform.event.ProxyEvent
import taboolib.platform.Sponge7Plugin

open class Sponge7ProxyEvent(val proxyEvent: ProxyEvent? = null) : AbstractEvent(), Cancellable {

    private var isCancelled = false

    open val allowCancelled: Boolean
        get() = true

    val eventContext: EventContext = EventContext.builder().add(EventContextKeys.PLUGIN, Sponge7Plugin.getInstance().pluginContainer).build()
    val eventCause: Cause = Cause.of(eventContext, Sponge7Plugin.getInstance().pluginContainer)

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
            error("Unsupported")
        }
    }

    override fun getCause(): Cause {
        return eventCause
    }

    fun call(): Boolean {
        Sponge.getEventManager().post(this)
        return !isCancelled
    }
}