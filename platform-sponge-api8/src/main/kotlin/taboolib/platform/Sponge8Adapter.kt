package taboolib.platform

import org.spongepowered.api.Sponge
import org.spongepowered.api.SystemSubject
import org.spongepowered.api.entity.living.player.server.ServerPlayer
import org.spongepowered.api.event.*
import org.spongepowered.api.event.impl.AbstractEvent
import taboolib.common.platform.*
import taboolib.platform.type.Sponge8Console
import taboolib.platform.type.Sponge8Player

/**
 * TabooLib
 * taboolib.platform.SpongeAdapter
 *
 * @author tr
 * @since 2021/6/21 17:02
 */
@Awake
@PlatformSide([Platform.SPONGE_API_8])
class Sponge8Adapter : PlatformAdapter {

    val plugin by lazy {
        Sponge8Plugin.getInstance()
    }

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
        return Sponge8Player(any as ServerPlayer)
    }

    override fun adaptCommandSender(any: Any): ProxyConsole {
        return Sponge8Console(any as SystemSubject)
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, order: EventOrder, beforeModifications: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = Sponge8Listener<Event> { func(it as T) }
        Sponge.eventManager().registerListener(plugin.pluginContainer, event as Class<Event>, Order.values()[order.ordinal], beforeModifications, listener)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        Sponge.eventManager().unregisterListeners(proxyListener)
    }

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = Sponge8Event(proxyEvent)
        Sponge.eventManager().post(event)
        event.proxyEvent.postCall()
    }

    class Sponge8Listener<T : Event>(val consumer: (Any) -> Unit) : EventListener<T>, ProxyListener {

        override fun handle(event: T) {
            consumer(event)
        }
    }

    class Sponge8Event(val proxyEvent: ProxyEvent) : AbstractEvent(), Cancellable {

        val eventContext: EventContext = EventContext.builder().add(EventContextKeys.PLUGIN, Sponge8Plugin.getInstance().pluginContainer).build()
        val eventCause: Cause = Cause.of(eventContext, Sponge8Plugin.getInstance().pluginContainer)

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