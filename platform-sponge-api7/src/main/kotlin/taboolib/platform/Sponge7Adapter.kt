package taboolib.platform

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Cancellable
import org.spongepowered.api.event.Event
import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.Order
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.EventContext
import org.spongepowered.api.event.cause.EventContextKeys
import org.spongepowered.api.event.impl.AbstractEvent
import taboolib.common.platform.*
import taboolib.platform.type.Sponge7CommandSender
import taboolib.platform.type.Sponge7Player

/**
 * TabooLib
 * taboolib.platform.SpongeAdapter
 *
 * @author tr
 * @since 2021/6/21 17:02
 */
@Awake
@PlatformSide([Platform.SPONGE_API_7])
class Sponge7Adapter : PlatformAdapter {

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return Sponge.getServer() as T
    }

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(Sponge.getServer().console)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Sponge.getServer().onlinePlayers.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return Sponge7Player(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is Player) adaptPlayer(any) else Sponge7CommandSender(any as CommandSource)
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, order: EventOrder, beforeModifications: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = Sponge7Listener<Event> { func(it as T) }
        val eventClass = if (ProxyEvent::class.java.isAssignableFrom(event)) Sponge7Event::class.java else event
        Sponge.getEventManager().registerListener(this, eventClass as Class<Event>, Order.values()[order.ordinal], beforeModifications, listener)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        Sponge.getEventManager().unregisterListeners(proxyListener)
    }

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = Sponge7Event(proxyEvent)
        Sponge.getEventManager().post(event)
        event.proxyEvent.postCall()
    }

    class Sponge7Listener<T : Event>(val consumer: (Any) -> Unit) : EventListener<T>, ProxyListener {

        override fun handle(event: T) {
            consumer(if (event is Sponge7Event) event.proxyEvent else event)
        }
    }

    class Sponge7Event(val proxyEvent: ProxyEvent) : AbstractEvent(), Cancellable {

        val eventContext: EventContext = EventContext.builder().add(EventContextKeys.PLUGIN, Sponge7Plugin.getInstance().pluginContainer).build()
        val eventCause: Cause = Cause.of(eventContext, Sponge7Plugin.getInstance().pluginContainer)

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

        override fun getCause(): Cause {
            return eventCause
        }
    }
}