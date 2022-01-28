package taboolib.common.platform.function

import taboolib.common.TabooLib
import taboolib.common.io.groupId
import taboolib.common.io.taboolibId
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.event.EventOrder
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.service.PlatformListener
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

fun <T> registerBukkitListener(
    event: Class<T>,
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = true,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    val closeableListener = CloseableListener()
    return PlatformFactory.getPlatformService<PlatformListener>().registerListener(event, priority, ignoreCancelled) { func(closeableListener, it) }.also {
        closeableListener.proxyListener = it
    }
}

fun <T> registerBungeeListener(
    event: Class<T>,
    level: Int = 0,
    ignoreCancelled: Boolean = false,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    val closeableListener = CloseableListener()
    return PlatformFactory.getPlatformService<PlatformListener>().registerListener(event, level, ignoreCancelled) { func(closeableListener, it) }.also {
        closeableListener.proxyListener = it
    }
}

fun <T> registerSpongeListener(
    event: Class<T>,
    order: EventOrder = EventOrder.DEFAULT,
    beforeModifications: Boolean = false,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    val closeableListener = CloseableListener()
    return PlatformFactory.getPlatformService<PlatformListener>().registerListener(event, order, beforeModifications) { func(closeableListener, it) }.also {
        closeableListener.proxyListener = it
    }
}

fun <T> registerVelocityListener(
    event: Class<T>,
    postOrder: PostOrder = PostOrder.NORMAL,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    val closeableListener = CloseableListener()
    return PlatformFactory.getPlatformService<PlatformListener>().registerListener(event, postOrder) { func(closeableListener, it) }.also {
        closeableListener.proxyListener = it
    }
}

fun unregisterListener(proxyListener: ProxyListener) {
    PlatformFactory.getPlatformService<PlatformListener>().unregisterListener(proxyListener)
}

internal class CloseableListener : Closeable {

    var proxyListener: ProxyListener? = null

    override fun close() {
        unregisterListener(proxyListener ?: error("close untimely"))
    }
}