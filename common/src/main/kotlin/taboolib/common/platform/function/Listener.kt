package taboolib.common.platform.function

import taboolib.common.io.groupId
import taboolib.common.io.taboolibId
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.event.EventOrder
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.service.PlatformListener
import java.util.concurrent.ConcurrentHashMap

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
    get() = platformClassCache.computeIfAbsent(this) {
        val superclass = superclass
        when {
            name.endsWith(proxyEventName) -> true
            superclass != null && superclass.name.endsWith(proxyEventName) -> true
            else -> superclass?.isPlatformEvent ?: false
        }
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