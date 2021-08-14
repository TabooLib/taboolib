package taboolib.platform

import cn.nukkit.Server
import cn.nukkit.command.CommandSender
import cn.nukkit.event.Cancellable
import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList
import cn.nukkit.event.Listener
import cn.nukkit.player.Player
import cn.nukkit.plugin.EventExecutor
import taboolib.common.platform.*
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.event.ProxyListener
import taboolib.platform.type.NukkitCommandSender
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

    private val plugin by lazy { NukkitPlugin.getInstance() }

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return Server.getInstance() as T
    }

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(Server.getInstance().consoleSender)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Server.getInstance().onlinePlayers.values.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return NukkitPlayer(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is Player) adaptPlayer(any) else NukkitCommandSender(any as CommandSender)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = NukkitListener(event as Class<Event>) { func(it as T) }
        val eventClass = if (ProxyEvent::class.java.isAssignableFrom(event)) NukkitEvent::class.java else event
        Server.getInstance().pluginManager.registerEvent(eventClass as Class<Event>, listener, priority.toNukkit(), listener, plugin, ignoreCancelled)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        HandlerList.unregisterAll(proxyListener as Listener)
    }

    override fun callEvent(proxyEvent: ProxyEvent) {
        Server.getInstance().pluginManager.callEvent(NukkitEvent(proxyEvent).also {
            it.proxyEvent.postCall()
        })
    }

    class NukkitListener(val clazz: Class<*>, val consumer: (Any) -> Unit) : Listener, EventExecutor, ProxyListener {

        override fun execute(listener: Listener, event: Event) {
            val origin: Any = if (event is NukkitEvent) event.proxyEvent else event
            if (origin.javaClass == clazz) {
                consumer(origin)
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