package taboolib.platform

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import taboolib.common.platform.*
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.platform.type.BukkitCommandSender
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

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return Bukkit.getServer() as T
    }

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(Bukkit.getConsoleSender())
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Bukkit.getWorlds().flatMap { it.players }.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return BukkitPlayer(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is Player) adaptPlayer(any) else BukkitCommandSender(any as CommandSender)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = BukkitListener(event as Class<Event>) { func(it as T) }
        val eventClass = if (ProxyEvent::class.java.isAssignableFrom(event)) BukkitEvent::class.java else event
        Bukkit.getPluginManager().registerEvent(eventClass as Class<Event>, listener, priority.toBukkit(), listener, plugin, ignoreCancelled)
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

    class BukkitListener(val clazz: Class<*>, val consumer: (Any) -> Unit) : Listener, EventExecutor, ProxyListener {

        override fun execute(listener: Listener, event: Event) {
            val origin: Any = if (event is BukkitEvent) event.proxyEvent else event
            if (origin.javaClass == clazz) {
                consumer(origin)
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
            fun getHandlerList(): HandlerList {
                return handlers
            }
        }
    }
}