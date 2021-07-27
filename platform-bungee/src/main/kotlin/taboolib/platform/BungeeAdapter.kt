package taboolib.platform

import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Cancellable
import net.md_5.bungee.api.plugin.Event
import net.md_5.bungee.event.EventHandlerMethod
import taboolib.common.platform.*
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.platform.type.BungeeCommandSender
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
@PlatformSide([Platform.BUNGEE])
class BungeeAdapter : PlatformAdapter {

    val plugin = BungeePlugin.getInstance()

    val eventBus by lazy {
        BungeeCord.getInstance().pluginManager.getProperty<EventBus>("eventBus")!!
    }

    val byListenerAndPriority by lazy {
        eventBus.getProperty<MutableMap<Class<*>, MutableMap<Byte, MutableMap<Any, Array<Method>>>>>("byListenerAndPriority")!!
    }

    val byEventBaked by lazy {
        eventBus.getProperty<MutableMap<Class<*>, Array<EventHandlerMethod>>>("byEventBaked")!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return plugin.proxy as T
    }

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(plugin.proxy.console)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return plugin.proxy.players.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return BungeePlayer(any as ProxiedPlayer)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is ProxyPlayer) adaptPlayer(any) else BungeeCommandSender(any as CommandSender)
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, level: Int, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        val listener = BungeeListener(event, level) { func(it as T) }
        var array = emptyArray<EventHandlerMethod>()
        val eventClass = if (ProxyEvent::class.java.isAssignableFrom(event)) BungeeEvent::class.java else event
        byListenerAndPriority.computeIfAbsent(eventClass) { HashMap() }.run {
            computeIfAbsent(level.toByte()) { HashMap() }.run {
                put(listener, arrayOf(BungeeListener.method))
                forEach { (listener, methods) -> methods.forEach { array += EventHandlerMethod(listener, it) } }
            }
        }
        byEventBaked[eventClass] = array
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        val listener = proxyListener as BungeeListener
        var array = emptyArray<EventHandlerMethod>()
        val eventClass = if (ProxyEvent::class.java.isAssignableFrom(listener.clazz)) BungeeEvent::class.java else listener.clazz
        byListenerAndPriority[eventClass]?.run {
            get(listener.level.toByte())?.run {
                remove(listener)
                forEach { (listener, methods) -> methods.forEach { array += EventHandlerMethod(listener, it) } }
            }
        }
        byEventBaked[eventClass] = array
    }

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = BungeeEvent(proxyEvent)
        plugin.proxy.pluginManager.callEvent(event)
        event.proxyEvent.postCall()
    }

    class BungeeListener(val clazz: Class<*>, val level: Int, val consumer: (Any) -> Unit) : ProxyListener {

        fun handle(event: Any) {
            val origin: Any = if (event is BungeeEvent) event.proxyEvent else event
            if (origin.javaClass == clazz) {
                consumer(origin)
            }
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