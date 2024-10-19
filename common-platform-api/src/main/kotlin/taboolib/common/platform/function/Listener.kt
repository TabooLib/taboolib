package taboolib.common.platform.function

import taboolib.common.event.InternalEventBus
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.service.PlatformListener
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 判定一个类是否在当前插件内被监听
 */
fun Class<*>.isListened(): Boolean {
    return listenEvents.contains(this) || InternalEventBus.isListening(this)
}

/**
 * 注册一个 Bukkit/Nukkit 监听器
 *
 * @param event 事件
 * @param priority 优先级
 * @param ignoreCancelled 是否忽略取消事件
 * @param func 事件处理函数
 * @return 监听器
 */
fun <T> registerBukkitListener(
    event: Class<T>,
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = true,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    listenEvents += event
    val closeableListener = CloseableListener()
    return PlatformFactory.getService<PlatformListener>().registerListener(event, priority, ignoreCancelled) { func(closeableListener, it) }.also {
        closeableListener.proxyListener = it
    }
}

/**
 * 注册一个 BungeeCord 监听器
 *
 * @param event 事件
 * @param level 优先级
 * @param ignoreCancelled 是否忽略取消事件
 * @param func 事件处理函数
 * @return 监听器
 */
fun <T> registerBungeeListener(
    event: Class<T>,
    level: Int = 0,
    ignoreCancelled: Boolean = false,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    listenEvents += event
    val closeableListener = CloseableListener()
    return PlatformFactory.getService<PlatformListener>().registerListener(event, level, ignoreCancelled) { func(closeableListener, it) }.also {
        closeableListener.proxyListener = it
    }
}

/**
 * 注册一个 AfyBroker 监听器
 *
 * @param event 事件
 * @param level 优先级
 * @param ignoreCancelled 是否忽略取消事件
 * @param func 事件处理函数
 * @return 监听器
 */
fun <T> registerAfyBrokerListener(
    event: Class<T>,
    level: Int = 0,
    ignoreCancelled: Boolean = false,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    listenEvents += event
    val closeableListener = CloseableListener()
    return PlatformFactory.getService<PlatformListener>().registerListener(event, level, ignoreCancelled) { func(closeableListener, it) }.also {
        closeableListener.proxyListener = it
    }
}

/**
 * 注册一个 Velocity 监听器
 *
 * @param event 事件
 * @param postOrder 优先级
 * @param func 事件处理函数
 * @return 监听器
 */
fun <T> registerVelocityListener(
    event: Class<T>,
    postOrder: PostOrder = PostOrder.NORMAL,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    listenEvents += event
    val closeableListener = CloseableListener()
    return PlatformFactory.getService<PlatformListener>().registerListener(event, postOrder) { func(closeableListener, it) }.also {
        closeableListener.proxyListener = it
    }
}

/**
 * 注销一个监听器
 */
fun unregisterListener(proxyListener: ProxyListener) {
    PlatformFactory.getService<PlatformListener>().unregisterListener(proxyListener)
}

/**
 * 当前插件正在监听的事件
 */
private val listenEvents = CopyOnWriteArraySet<Class<*>>()

/**
 * 监听器包装类型
 */
private class CloseableListener : Closeable {

    var proxyListener: ProxyListener? = null

    override fun close() {
        unregisterListener(proxyListener ?: error("close untimely"))
    }
}