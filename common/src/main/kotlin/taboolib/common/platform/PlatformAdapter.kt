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

    fun adapterPlayer(any: Any): ProxyPlayer

    fun adapterCommandSender(any: Any): ProxyCommandSender

    fun <T> registerListener(event: Class<T>, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = true, func: (T) -> Unit): ProxyListener

    fun unregisterListener(proxyListener: ProxyListener)

    fun callEvent(proxyEvent: ProxyEvent)
}