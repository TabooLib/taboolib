package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.service.PlatformAdapter
import java.util.*

fun console(): ProxyCommandSender {
    return PlatformFactory.getService<PlatformAdapter>().console()
}

fun adaptCommandSender(any: Any): ProxyCommandSender {
    return PlatformFactory.getService<PlatformAdapter>().adaptCommandSender(any)
}

fun onlinePlayers(): List<ProxyPlayer> {
    return PlatformFactory.getService<PlatformAdapter>().onlinePlayers()
}

fun adaptPlayer(any: Any): ProxyPlayer {
    return PlatformFactory.getService<PlatformAdapter>().adaptPlayer(any)
}

fun getProxyPlayer(name: String): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.name == name }
}

fun getProxyPlayer(uuid: UUID): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.uniqueId == uuid }
}