package taboolib.platform

import com.velocitypowered.api.event.EventHandler
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import taboolib.common.platform.*
import taboolib.platform.type.VelocityConsole
import taboolib.platform.type.VelocityPlayer

/**
 * TabooLib
 * taboolib.platform.VelocityAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@PlatformSide([Platform.VELOCITY])
class VelocityAdapter : PlatformAdapter {

    val plugin = VelocityPlugin.getInstance()

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return plugin.server as T
    }

    override fun console(): ProxyConsole {
        return adaptCommandSender(plugin.server.consoleCommandSource)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return plugin.server.allPlayers.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return VelocityPlayer(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyConsole {
        return VelocityConsole(any as ConsoleCommandSource)
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, postOrder: PostOrder, func: (T) -> Unit): ProxyListener {
        val listener = VelocityListener<T> { func(it) }
        plugin.server.eventManager.register(this, event, com.velocitypowered.api.event.PostOrder.values()[postOrder.ordinal], listener)
        return listener
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        plugin.server.eventManager.unregister(this, proxyListener as EventHandler<*>)
    }

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = VelocityEvent(proxyEvent)
        plugin.server.eventManager.fire(event)
        event.proxyEvent.postCall()
    }

    class VelocityListener<T>(val consumer: (T) -> Unit) : ProxyListener, EventHandler<T> {

        override fun execute(event: T) {
            consumer(event)
        }
    }

    class VelocityEvent(val proxyEvent: ProxyEvent) : ResultedEvent<GenericResult> {

        override fun getResult(): GenericResult {
            return if (proxyEvent.isCancelled) GenericResult.denied() else GenericResult.allowed()
        }

        override fun setResult(result: GenericResult) {
            if (proxyEvent.allowCancelled) {
                proxyEvent.isCancelled = !result.isAllowed
            } else {
                error("unsupported")
            }
        }
    }
}