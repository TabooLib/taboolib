package taboolib.platform

import org.spongepowered.api.Server
import org.spongepowered.api.Sponge
import org.spongepowered.api.SystemSubject
import org.spongepowered.api.event.*
import org.spongepowered.api.event.impl.AbstractEvent
import taboolib.common.platform.*
import taboolib.platform.type.SpongeConsole

/**
 * TabooLib
 * taboolib.platform.SpongeAdapter
 *
 * @author tr
 * @since 2021/6/21 17:02
 */
@Awake
@PlatformSide([Platform.SPONGE_API_8])
class SpongeAdapter : PlatformAdapter {

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return Sponge.server() as T
    }

    override fun console(): ProxyConsole {
        return adaptCommandSender(Sponge.systemSubject())
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Sponge.server().onlinePlayers().map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        error("unsupported ap7->api8")
    }

    override fun adaptCommandSender(any: Any): ProxyConsole {
        return SpongeConsole(any as SystemSubject)
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, order: EventOrder, beforeModifications: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        error("unsupported")
    }

    override fun callEvent(proxyEvent: ProxyEvent) {
        error("unsupported")
    }

    class SpongeListener<T : Event>(val consumer: (Any) -> Unit) : EventListener<T>, ProxyListener {

        override fun handle(event: T) {
            consumer(event)
        }
    }

    class SpongeEvent(val proxyEvent: ProxyEvent) : AbstractEvent(), Cancellable {

        val eventContext: EventContext = EventContext.builder().add(EventContextKeys.PLUGIN, SpongePlugin.getInstance().pluginContainer).build()
        val eventCause: Cause = Cause.of(eventContext, SpongePlugin.getInstance().pluginContainer)

        override fun isCancelled(): Boolean {
            return proxyEvent.isCancelled
        }

        override fun setCancelled(value: Boolean) {
            if (proxyEvent.allowCancelled) {
                proxyEvent.isCancelled = value
            } else {
                error("unsupported")
            }
        }

        override fun cause(): Cause {
            return eventCause
        }
    }
}