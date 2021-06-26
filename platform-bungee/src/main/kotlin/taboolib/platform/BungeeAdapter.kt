package taboolib.platform

import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Cancellable
import net.md_5.bungee.api.plugin.Event
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.command.ConsoleCommandSender
import net.md_5.bungee.event.EventBus
import net.md_5.bungee.event.EventHandlerMethod
import taboolib.common.platform.*
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.common.reflect.Reflex.Companion.reflexInvoke
import taboolib.platform.type.BungeeConsole
import taboolib.platform.type.BungeePlayer
import java.lang.reflect.Method

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

    val plugin = BungeePlugin.getInstance()

    val eventBus by lazy {
        BungeeCord.getInstance().pluginManager.reflex<EventBus>("eventBus")!!
    }

    val byListenerAndPriority by lazy {
        eventBus.reflex<MutableMap<Class<*>, MutableMap<Byte, MutableMap<Any, Array<Method>>>>>("byListenerAndPriority")!!
    }

    val byEventBaked by lazy {
        eventBus.reflex<MutableMap<Class<*>, Array<EventHandlerMethod>>>("byEventBaked")!!
    }

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

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, level: Int, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = BungeeListener(event, level) { func(it as T) }
        var array = emptyArray<EventHandlerMethod>()
        byListenerAndPriority.computeIfAbsent(event::class.java) { HashMap() }.run {
            computeIfAbsent(level.toByte()) { HashMap() }.run {
                put(listener, arrayOf(BungeeListener.method))
                forEach { (listener, methods) -> methods.forEach { array += EventHandlerMethod(listener, it) } }
            }
        }
        byEventBaked[event::class.java] = array
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        val listener = proxyListener as BungeeListener
        var array = emptyArray<EventHandlerMethod>()
        byListenerAndPriority[listener.event]?.run {
            get(listener.level.toByte())?.run {
                remove(listener)
                forEach { (listener, methods) -> methods.forEach { array += EventHandlerMethod(listener, it) } }
            }
        }
        byEventBaked[listener.event] = array
    }

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = BungeeEvent(proxyEvent)
        plugin.proxy.pluginManager.callEvent(event)
        event.proxyEvent.postCall()
    }

    class BungeeListener(val event: Class<*>, val level: Int, val consumer: (Any) -> Unit) : ProxyListener {

        fun handle(event: Any) {
            consumer(event)
        }

        companion object {

            val method: Method = BungeeListener::class.java.getDeclaredMethod("handle", Any::class.java).also {
                it.isAccessible = true
            }
        }
    }

    class BungeeEvent(val proxyEvent: ProxyEvent) : Event(), Cancellable {

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
    }
}