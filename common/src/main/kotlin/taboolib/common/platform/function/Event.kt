package taboolib.common.platform.function

import taboolib.common.io.groupId
import taboolib.common.io.taboolibId
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent

fun callEvent(proxyEvent: ProxyEvent) {
    PlatformFactory.getService<PlatformEvent>().callEvent(proxyEvent)
}

private val proxyEventName = "platform.type.${runningPlatform.key}ProxyEvent"

private val platformEventName = "${taboolibId}.platform.type.${runningPlatform.key}ProxyEvent"

private val Class<*>.isProxyEvent: Boolean
    get() = proxyEvent != null

private val Class<*>.proxyEvent: Class<*>?
    get() {
        val superclass = superclass
        return if (superclass != null && superclass.name.endsWith("platform.event.ProxyEvent")) superclass else superclass?.proxyEvent
    }

val Class<*>.isPlatformEvent: Boolean
    get() {
        val superclass = superclass
        return when {
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