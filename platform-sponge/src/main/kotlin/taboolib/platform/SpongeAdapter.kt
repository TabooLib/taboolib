package taboolib.platform

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.source.ConsoleSource
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Cancellable
import org.spongepowered.api.event.Event
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.EventContext
import taboolib.common.platform.*
import taboolib.platform.type.SpongeConsole
import taboolib.platform.type.SpongePlayer

/**
 * TabooLib
 * taboolib.platform.SpongeAdapter
 *
 * @author tr
 * @since 2021/6/21 17:02
 */
@Awake
@PlatformSide([Platform.SPONGE])
class SpongeAdapter : PlatformAdapter {

    private val plugin = SpongePlugin.instance

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return Sponge.getServer() as T
    }

    override fun console(): ProxyConsole {
        return adaptCommandSender(Sponge.getServer().console)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Sponge.getServer().onlinePlayers.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return SpongePlayer(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyConsole {
        return SpongeConsole(any as ConsoleSource)
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        TODO("Not yet implemented")
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        Sponge.getEventManager().unregisterListeners(proxyListener as Event)
    }

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = SpongeEvent(proxyEvent)
        Sponge.getEventManager().post(event)
        event.proxyEvent.postCall()
    }

//    class SpongeListener(val clazz: Class<*>, val predicate: (Any) -> Boolean, val consumer: (Any) -> Unit) : Listener, EventExecutor, ProxyListener {
//
//        override fun execute(listener: Listener, event: Event) {
//            try {
//                val cast = clazz.cast(event)
//                if (predicate(cast)) {
//                    consumer(cast)
//                }
//            } catch (ignore: ClassCastException) {
//            } catch (e: Throwable) {
//                e.printStackTrace()
//            }
//        }
//    }

    class SpongeEvent(val proxyEvent: ProxyEvent) : Event, Cancellable {

        override fun isCancelled(): Boolean {
            return proxyEvent.isCancelled
        }

        override fun setCancelled(value: Boolean) {
            if (proxyEvent.allowCancelled) {
                proxyEvent.isCancelled = value
            } else {
                error("not cancellable")
            }
        }

        // TODO: Maybe it's has some problems? idk.
        override fun getCause(): Cause {
            return Cause.of(EventContext.empty(), "")
        }
    }
}