package taboolib.platform

import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Cancellable
import net.md_5.bungee.api.plugin.Event
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.command.ConsoleCommandSender
import net.md_5.bungee.event.EventBus
import taboolib.common.platform.*
import taboolib.platform.type.BungeeConsole
import taboolib.platform.type.BungeePlayer

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@PlatformSide([Platform.BUKKIT])
class BungeeAdapter : PlatformAdapter {

    val plugin = BungeePlugin.instance

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return plugin.proxy as T
    }

    override fun console(): ProxyConsole {
        return adaptCommandSender(plugin.proxy.console)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return plugin.proxy.players.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return BungeePlayer(any as ProxiedPlayer)
    }

    override fun adaptCommandSender(any: Any): ProxyConsole {
        return BungeeConsole(any as ConsoleCommandSender)
    }

    // TODO: Fix these s**t
    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        TODO("Not yet implemented")
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
    }

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = BungeeEvent(proxyEvent)
        plugin.proxy.pluginManager.callEvent(event)
        event.proxyEvent.postCall()
    }

    class BungeeListener(val clazz: Class<*>, val predicate: (Any) -> Boolean, val consumer: (Any) -> Unit) : Listener, EventBus(), ProxyListener {

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
    }

    class BungeeEvent(val proxyEvent: ProxyEvent) : Event(), Cancellable {

//        init {
//            if (proxyEvent.allowAsynchronous) {
//                reflex("async", !isPrimaryThread)
//            }
//        }

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
    }
}