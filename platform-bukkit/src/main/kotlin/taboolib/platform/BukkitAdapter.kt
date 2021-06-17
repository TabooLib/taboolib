package taboolib.platform

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.ServerCommandEvent
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.*
import taboolib.common.util.Location
import taboolib.common5.reflect.Reflex.Companion.reflex
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.platform.BukkitAdapter
 *
 * @author sky
 * @since 2021/6/17 12:22 上午
 */
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
        val player = any as Player
        return object : ProxyPlayer {

            override val origin: Any
                get() = player

            override val name: String
                get() = player.name

            override val address: InetSocketAddress?
                get() = player.address

            override val uniqueId: UUID
                get() = player.uniqueId

            override val ping: Int
                get() = player.ping

            override val locale: String
                get() = player.locale

            override val world: String
                get() = player.world.name

            override val location: Location
                get() = Location(world, player.location.x, player.location.y, player.location.z, player.location.yaw, player.location.pitch)

            override fun kick(message: String?) {
                player.kickPlayer(message)
            }

            override fun chat(message: String) {
                player.chat(message)
            }

            override fun sendRawMessage(message: String) {
                player.sendRawMessage(message)
            }

            override fun sendMessage(message: String) {
                player.sendMessage(message)
            }

            override fun performCommand(command: String): Boolean {
                return dispatchCommand(player, command)
            }

            override fun hasPermission(permission: String): Boolean {
                return player.hasPermission(permission)
            }

            override fun teleport(loc: Location) {
                player.teleport(org.bukkit.Location(Bukkit.getWorld(loc.world!!), loc.x, loc.y, loc.z, loc.yaw, loc.pitch))
            }
        }
    }

    override fun adaptCommandSender(any: Any): ProxyConsole {
        val sender = any as ConsoleCommandSender
        return object : ProxyConsole {

            override val origin: Any
                get() = sender

            override val name: String
                get() = sender.name

            override fun sendMessage(message: String) {
                sender.sendMessage(message)
            }

            override fun performCommand(command: String): Boolean {
                return dispatchCommand(sender, command)
            }

            override fun hasPermission(permission: String): Boolean {
                return sender.hasPermission(permission)
            }
        }
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

    fun dispatchCommand(sender: CommandSender?, command: String): Boolean {
        if (sender is Player) {
            val event = PlayerCommandPreprocessEvent((sender as Player?)!!, "/$command")
            Bukkit.getPluginManager().callEvent(event)
            if (!event.isCancelled && event.message.isNotBlank() && event.message.startsWith("/")) {
                return Bukkit.dispatchCommand(event.player, event.message.substring(1))
            }
        } else {
            val e = ServerCommandEvent(sender!!, command)
            Bukkit.getPluginManager().callEvent(e)
            if (!e.isCancelled && e.command.isNotBlank()) {
                return Bukkit.dispatchCommand(e.sender, e.command)
            }
        }
        return false
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