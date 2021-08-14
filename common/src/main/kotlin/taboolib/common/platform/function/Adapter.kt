package taboolib.common.platform.function

import taboolib.common.platform.PlatformAdapter
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.event.*
import java.util.*

fun <T> server(): T {
    return PlatformFactory.getService<PlatformAdapter>().server()
}

fun console(): ProxyCommandSender {
    return PlatformFactory.getService<PlatformAdapter>().console()
}

fun adaptCommandSender(any: Any): ProxyCommandSender {
    return PlatformFactory.getService<PlatformAdapter>().adaptCommandSender(any)
}

fun onlinePlayers(): List<ProxyPlayer> {
    return PlatformFactory.getService<PlatformAdapter>().onlinePlayers()
}

fun adaptPlayer(any: Any): ProxyPlayer {
    return PlatformFactory.getService<PlatformAdapter>().adaptPlayer(any)
}

fun getProxyPlayer(name: String): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.name == name }
}

fun getProxyPlayer(uuid: UUID): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.uniqueId == uuid }
}

fun <T> registerListener(event: Class<T>, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = true, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformAdapter>().registerListener(event, priority, ignoreCancelled, func)
}

fun <T> registerListener(event: Class<T>, level: Int = 0, ignoreCancelled: Boolean = false, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformAdapter>().registerListener(event, level, ignoreCancelled, func)
}

fun <T> registerListener(event: Class<T>, postOrder: PostOrder = PostOrder.NORMAL, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformAdapter>().registerListener(event, postOrder, func)
}

fun <T> registerListener(event: Class<T>, order: EventOrder = EventOrder.DEFAULT, beforeModifications: Boolean = false, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformAdapter>().registerListener(event, order, beforeModifications, func)
}

fun unregisterListener(proxyListener: ProxyListener) {
    PlatformFactory.getService<PlatformAdapter>().unregisterListener(proxyListener)
}

fun callEvent(proxyEvent: ProxyEvent) {
    PlatformFactory.getService<PlatformAdapter>().callEvent(proxyEvent)
}