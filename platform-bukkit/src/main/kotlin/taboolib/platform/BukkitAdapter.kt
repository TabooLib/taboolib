package taboolib.platform

import org.bukkit.Bukkit
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.*
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.platform.type.BukkitConsole
import taboolib.platform.type.BukkitPlayer
import taboolib.platform.util.toBukkit

/**
 * TabooLib
 * taboolib.platform.BukkitAdapter
 *
 * @author sky
 * @since 2021/6/17 12:22 上午
 */
@Awake
@PlatformSide([Platform.BUKKIT])
class BukkitAdapter : PlatformAdapter {

    val plugin by lazy {
        JavaPlugin.getProvidingPlugin(BukkitIO::class.java) as BukkitPlugin
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return Bukkit.getServer() as T
    }

    override fun console(): ProxyConsole {
        return adaptCommandSender(Bukkit.getConsoleSender())
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Bukkit.getOnlinePlayers().map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return BukkitPlayer(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyConsole {
        return BukkitConsole(any as ConsoleCommandSender)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = BukkitListener(event as Class<Event>,
            predicate = {
                event == if (it is BukkitEvent) it.proxyEvent::class.java else it.javaClass
            },
            consumer = {
                func(it as T)
            })
        Bukkit.getPluginManager().registerEvent(event, listener, priority.toBukkit(), listener, plugin, ignoreCancelled)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        HandlerList.unregisterAll(proxyListener as Listener)
    }

    override fun callEvent(proxyEvent: ProxyEvent) {
        val bukkitEvent = BukkitEvent(proxyEvent)
        Bukkit.getPluginManager().callEvent(bukkitEvent)
        bukkitEvent.proxyEvent.postCall()
    }

    class BukkitListener(val clazz: Class<*>, val predicate: (Any) -> Boolean, val consumer: (Any) -> Unit) : Listener, EventExecutor, ProxyListener {

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

    class BukkitEvent(val proxyEvent: ProxyEvent) : Event(), Cancellable {

        init {
            if (proxyEvent.allowAsynchronous) {
                reflex("async", !isPrimaryThread)
            }
        }

        override fun getHandlers(): HandlerList {
            return getHandlerList()
        }

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