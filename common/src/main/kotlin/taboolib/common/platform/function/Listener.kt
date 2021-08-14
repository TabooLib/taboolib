package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.event.EventOrder
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.service.PlatformListener

fun <T> registerListener(event: Class<T>, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = true, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformListener>().registerListener(event, priority, ignoreCancelled, func)
}

fun <T> registerListener(event: Class<T>, level: Int = 0, ignoreCancelled: Boolean = false, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformListener>().registerListener(event, level, ignoreCancelled, func)
}

fun <T> registerListener(event: Class<T>, postOrder: PostOrder = PostOrder.NORMAL, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformListener>().registerListener(event, postOrder, func)
}

fun <T> registerListener(event: Class<T>, order: EventOrder = EventOrder.DEFAULT, beforeModifications: Boolean = false, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformListener>().registerListener(event, order, beforeModifications, func)
}

fun unregisterListener(proxyListener: ProxyListener) {
    PlatformFactory.getService<PlatformListener>().unregisterListener(proxyListener)
}