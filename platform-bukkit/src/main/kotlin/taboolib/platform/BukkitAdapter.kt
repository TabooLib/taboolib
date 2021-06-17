package taboolib.platform

import org.bukkit.Bukkit
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.*
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.platform.type.BukkitConsole
import taboolib.platform.type.BukkitPlayer

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

    private val plugin = JavaPlugin.getProvidingPlugin(BukkitIO::class.java) as BukkitPlugin

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
        val bukkitListener = BukkitListener(event as Class<Event>, { it.javaClass == event }, { func(it as T) })
        val bukkitPriority = when (priority) {
            EventPriority.LOWEST -> org.bukkit.event.EventPriority.LOWEST
            EventPriority.LOW -> org.bukkit.event.EventPriority.LOW
            EventPriority.NORMAL -> org.bukkit.event.EventPriority.NORMAL
            EventPriority.HIGH -> org.bukkit.event.EventPriority.HIGH
            EventPriority.HIGHEST -> org.bukkit.event.EventPriority.HIGHEST
            EventPriority.MONITOR, EventPriority.CUSTOM -> org.bukkit.event.EventPriority.MONITOR
        }
        Bukkit.getPluginManager().registerEvent(event, bukkitListener, bukkitPriority, bukkitListener, plugin, ignoreCancelled)
        return bukkitListener
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