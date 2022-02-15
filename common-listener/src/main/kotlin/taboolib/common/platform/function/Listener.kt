package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.event.EventOrder
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.service.PlatformListener
import taboolib.internal.RegisteredListener
import java.io.Closeable

fun <T> registerBukkitListener(
    event: Class<T>,
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = true,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    val registeredListener = RegisteredListener()

    return PlatformFactory
        .getPlatformService<PlatformListener>()
        .registerListener(event, priority, ignoreCancelled) { func(registeredListener, it) }
        .also { registeredListener.listener = it }
}

fun <T> registerBungeeListener(
    event: Class<T>,
    level: Int = 0,
    ignoreCancelled: Boolean = false,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    val registeredListener = RegisteredListener()

    return PlatformFactory
        .getPlatformService<PlatformListener>()
        .registerListener(event, level, ignoreCancelled) { func(registeredListener, it) }
        .also { registeredListener.listener = it }
}

fun <T> registerSpongeListener(
    event: Class<T>,
    order: EventOrder = EventOrder.DEFAULT,
    beforeModifications: Boolean = false,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    val registeredListener = RegisteredListener()

    return PlatformFactory.getPlatformService<PlatformListener>()
        .registerListener(event, order, beforeModifications) { func(registeredListener, it) }
        .also { registeredListener.listener = it }
}

fun <T> registerVelocityListener(
    event: Class<T>,
    postOrder: PostOrder = PostOrder.NORMAL,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    val registeredListener = RegisteredListener()

    return PlatformFactory
        .getPlatformService<PlatformListener>()
        .registerListener(event, postOrder) { func(registeredListener, it) }
        .also { registeredListener.listener = it }
}

fun unregisterListener(proxyListener: ProxyListener) {
    PlatformFactory.getPlatformService<PlatformListener>().unregisterListener(proxyListener)
}
