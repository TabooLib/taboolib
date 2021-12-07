package taboolib.common.platform.function

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
    return PlatformFactory.getService<PlatformListener>().registerListener(event, priority, ignoreCancelled) { func(closeableListener, it) }.also {
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
    return PlatformFactory.getService<PlatformListener>().registerListener(event, level, ignoreCancelled) { func(closeableListener, it) }.also {
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
    return PlatformFactory.getService<PlatformListener>().registerListener(event, order, beforeModifications) { func(closeableListener, it) }.also {
        closeableListener.proxyListener = it
    }
}

fun <T> registerVelocityListener(
    event: Class<T>,
    postOrder: PostOrder = PostOrder.NORMAL,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    val closeableListener = CloseableListener()
    return PlatformFactory.getService<PlatformListener>().registerListener(event, postOrder) { func(closeableListener, it) }.also {
        closeableListener.proxyListener = it
    }
}

fun unregisterListener(proxyListener: ProxyListener) {
    PlatformFactory.getService<PlatformListener>().unregisterListener(proxyListener)
}

private val proxyEventName = "platform.type.${runningPlatform.key}ProxyEvent"

private val platformEventName = "$taboolibId.platform.type.${runningPlatform.key}ProxyEvent"

private val platformClassCache = ConcurrentHashMap<Class<*>, Boolean>()

private val Class<*>.isProxyEvent: Boolean
    get() = proxyEvent != null

private val Class<*>.proxyEvent: Class<*>?
    get() {
        val superclass = superclass
        return if (superclass != null && superclass.name.endsWith("platform.event.ProxyEvent")) superclass else superclass?.proxyEvent
    }

/**
 * 是否为跨平台事件的子平台实现
 * 而非跨平台事件的总接口（ProxyEvent）
 */
val Class<*>.isPlatformEvent: Boolean
    get() {
        if (!platformClassCache.containsKey(this)) {
            val superclass = superclass
            platformClassCache[this] = when {
                name.endsWith(proxyEventName) -> true
                superclass != null && superclass.name.endsWith(proxyEventName) -> true
                else -> superclass?.isPlatformEvent ?: false
            }
        }
        return platformClassCache[this]!!
    }

fun Class<*>.getUsableEvent(): Class<*> {
    val event = proxyEvent
    return if (event != null) {
        try {
            Class.forName("${event.groupId}.$platformEventName")
        } catch (ignored: ClassNotFoundException) {
            error("Unable to register listener $name")
        }
    } else {
        this
    }
}

internal class CloseableListener : Closeable {

    var proxyListener: ProxyListener? = null

    override fun close() {
        unregisterListener(proxyListener ?: error("close untimely"))
    }
}