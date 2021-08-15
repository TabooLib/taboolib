package taboolib.common.platform.function

import taboolib.common.io.groupId
import taboolib.common.io.taboolibId
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent

fun callEvent(proxyEvent: ProxyEvent) {
    PlatformFactory.getService<PlatformEvent>().callEvent(proxyEvent)
}

val Class<*>.isProxyEvent: Boolean
    get() = if (superclass != null && superclass.name == "$groupId.$taboolibId.common.platform.event.ProxyEvent") {
        true
    } else {
        superclass?.isProxyEvent ?: false
    }

val Class<*>.isPlatformEvent: Boolean
    get() = if (superclass != null && superclass.name == "$groupId.$taboolibId.platform.type.${runningPlatform.key}ProxyEvent") {
        true
    } else {
        superclass?.isProxyEvent ?: false
    }

fun Class<*>.getPlatformEvent(): Class<*> {
    return if (isProxyEvent) {
        try {
            Class.forName("${groupId}.$taboolibId.platform.type.${runningPlatform.key}ProxyEvent")
        } catch (ignored: ClassNotFoundException) {
            error("Unable to register listener $name")
        }
    } else {
        this
    }
}