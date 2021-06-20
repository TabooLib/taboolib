package taboolib.platform

import cn.nukkit.command.ConsoleCommandSender
import cn.nukkit.event.Cancellable
import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList
import cn.nukkit.event.Listener
import cn.nukkit.player.Player
import cn.nukkit.plugin.EventExecutor
import taboolib.common.platform.*
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.platform.type.NukkitConsole
import taboolib.platform.type.NukkitPlayer
import taboolib.platform.util.toNukkit

/**
 * TabooLib
 * taboolib.platform.NukkitAdapter
 *
 * @author CziSKY
 * @since 2021/6/20 0:46
 */
@Awake
@PlatformSide([Platform.NUKKIT])
class NukkitAdapter : PlatformAdapter {

    private val plugin = NukkitPlugin.instance

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return plugin.server as T
    }

    override fun console(): ProxyConsole {
        return adaptCommandSender(plugin.server.consoleSender)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return plugin.server.onlinePlayers.values.map {
            adaptPlayer(it)
        }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return NukkitPlayer(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyConsole {
        return NukkitConsole(any as ConsoleCommandSender)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = NukkitListener(event as Class<Event>,
            predicate = {
                event == if (it is NukkitEvent) it.proxyEvent::class.java else it.javaClass
            },
            consumer = {
                func(it as T)
            })
        plugin.server.pluginManager.registerEvent(event, listener, priority.toNukkit(), listener, plugin, ignoreCancelled)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        HandlerList.unregisterAll(proxyListener as Listener)
    }

    override fun callEvent(proxyEvent: ProxyEvent) {
        plugin.server.pluginManager.callEvent(NukkitEvent(proxyEvent).also {
            it.proxyEvent.postCall()
        })
    }

    class NukkitListener(val clazz: Class<*>, val predicate: (Any) -> Boolean, val consumer: (Any) -> Unit) : Listener, EventExecutor, ProxyListener {

        override fun execute(listener: Listener, event: Event) {
            try {
                val cast = clazz.cast(event)
                if (predicate(cast)) {
                    consumer(cast)
                }
            } catch (ignore: ClassCastException) {
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    class NukkitEvent(val proxyEvent: ProxyEvent) : Event(), Cancellable {

        override fun isCancelled(): Boolean {
            return proxyEvent.isCancelled
        }

        override fun setCancelled(value: Boolean) {
            if (proxyEvent.allowCancelled) {
                proxyEvent.isCancelled = value
            } else {
                error("no cancellable")
            }
        }

        companion object {

            @JvmField
            val handlers = HandlerList()

            @JvmStatic
            private fun getHandlerList(): HandlerList {
                return handlers
            }
        }
    }
}