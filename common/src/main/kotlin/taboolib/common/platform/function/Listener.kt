package taboolib.common.platform.function

import taboolib.common.io.groupId
import taboolib.common.io.taboolibId
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.event.*
import taboolib.common.platform.service.PlatformListener
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 判定一个类是否在当前插件内被监听
 */
fun Class<*>.isListened(): Boolean {
    return listenEvents.contains(this)
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
 * 注册一个 Sponge 监听器
 *
 * @param event 事件
 * @param order 优先级
 * @param beforeModifications 是否在事件修改前触发
 * @param func 事件处理函数
 * @return 监听器
 */
fun <T> registerSpongeListener(
    event: Class<T>,
    order: EventOrder = EventOrder.DEFAULT,
    beforeModifications: Boolean = false,
    func: Closeable.(T) -> Unit,
): ProxyListener {
    listenEvents += event
    val closeableListener = CloseableListener()
    return PlatformFactory.getService<PlatformListener>().registerListener(event, order, beforeModifications) { func(closeableListener, it) }.also {
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

private val listenEvents = CopyOnWriteArraySet<Class<*>>()

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

private class CloseableListener : Closeable {

    var proxyListener: ProxyListener? = null

    override fun close() {
        unregisterListener(proxyListener ?: error("close untimely"))
    }
}