package taboolib.common.platform.function

import taboolib.common.TabooLib
import taboolib.common.io.groupId
import taboolib.common.io.taboolibId
import java.util.concurrent.ConcurrentHashMap

private val proxyEventName = "platform.type.${TabooLib.runningPlatform().key}ProxyEvent"

private val platformEventName = "$taboolibId.platform.type.${TabooLib.runningPlatform().key}ProxyEvent"

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