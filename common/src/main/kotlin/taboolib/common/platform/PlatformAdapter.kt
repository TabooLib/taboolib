package taboolib.common.platform

import taboolib.common.util.Location

/**
 * TabooLib
 * taboolib.common.platform.PlatformAdaptor
 *
 * @author sky
 * @since 2021/6/17 12:04 上午
 */
interface PlatformAdapter {

    fun <T> server(): T

    fun console(): ProxyConsole

    fun onlinePlayers(): List<ProxyPlayer>

    fun adaptPlayer(any: Any): ProxyPlayer

    fun adaptCommandSender(any: Any): ProxyCommandSender

    fun <T> registerListener(event: Class<T>, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = true, func: (T) -> Unit): ProxyListener

    fun <T> registerListener(event: Class<T>, order: EventOrder = EventOrder.DEFAULT, beforeModifications: Boolean = false, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    fun unregisterListener(proxyListener: ProxyListener)

    fun callEvent(proxyEvent: ProxyEvent)
}