package taboolib.common.platform.function

import taboolib.common.TabooLib
import taboolib.common.io.tabooLibPath
import java.util.concurrent.ConcurrentHashMap

/**
 * 参考：platform.type.BukkitProxyEvent
 */
private val platformEventName by lazy { "platform.type.${TabooLib.runningPlatform().key}ProxyEvent" }

private val platformClassCacheMap = ConcurrentHashMap<Class<*>, Boolean>()

/**
 * 是否为跨平台事件
 */
val Class<*>.isProxyEvent: Boolean
    get() = getProxyEventAbstractClass() != null

/**
 * 是否为子平台事件
 */
val Class<*>.isPlatformEvent: Boolean
    get() = platformClassCacheMap.computeIfAbsent(this) {
        when {
            // 类名以 platform.type.BukkitProxyEvent 结尾
            name.endsWith(platformEventName) -> true
            // 父类以 platform.type.BukkitProxyEvent 结尾
            superclass != null && superclass.name.endsWith(platformEventName) -> true
            // 对父类进行递归检查
            else -> superclass?.isPlatformEvent ?: false
        }
    }

/**
 * 获取用于注册监听器的代理类
 * 例如直接继承 Event 接口的 BukkitProxyEvent 类
 */
fun Class<*>.getEventClass(): Class<*> {
    val event = getProxyEventAbstractClass()
    return if (event != null) Class.forName("${event.tabooLibPath}.$platformEventName") else this
}

private fun Class<*>.getProxyEventAbstractClass(): Class<*>? {
    return if (superclass != null && superclass.name.endsWith("platform.event.ProxyEvent")) superclass else superclass?.getProxyEventAbstractClass()
}
