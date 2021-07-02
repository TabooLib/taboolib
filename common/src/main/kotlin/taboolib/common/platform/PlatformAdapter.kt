package taboolib.common.platform

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

    /**
     * bukkit & nukkit
     */
    fun <T> registerListener(event: Class<T>, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = true, func: (T) -> Unit): ProxyListener

    /**
     * bungeecord
     */
    fun <T> registerListener(event: Class<T>, level: Int, ignoreCancelled: Boolean = false, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    /**
     * velocity
     */
    fun <T> registerListener(event: Class<T>, postOrder: PostOrder = PostOrder.NORMAL, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    /**
     * sponge
     */
    fun <T> registerListener(event: Class<T>, order: EventOrder = EventOrder.DEFAULT, beforeModifications: Boolean = false, func: (T) -> Unit): ProxyListener {
        error("unsupported")
    }

    fun unregisterListener(proxyListener: ProxyListener)

    fun callEvent(proxyEvent: ProxyEvent)
}