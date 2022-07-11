package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import java.util.*

/**
 * 获取控制台
 */
fun console(): ProxyCommandSender {
    return PlatformFactory.getService<PlatformAdapter>().console()
}

/**
 * 将平台实现转换为跨平台实现
 */
fun adaptCommandSender(any: Any): ProxyCommandSender {
    return PlatformFactory.getService<PlatformAdapter>().adaptCommandSender(any)
}

/**
 * 获取所有在线玩家
 */
fun onlinePlayers(): List<ProxyPlayer> {
    return PlatformFactory.getService<PlatformAdapter>().onlinePlayers()
}

/**
 * 将平台实现转换为跨平台实现
 */
fun adaptPlayer(any: Any): ProxyPlayer {
    return PlatformFactory.getService<PlatformAdapter>().adaptPlayer(any)
}

/**
 * 通过名称获取玩家
 */
fun getProxyPlayer(name: String): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.name == name }
}

/**
 * 通过 UUID 获取玩家
 */
fun getProxyPlayer(uuid: UUID): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.uniqueId == uuid }
}

fun adaptLocation(any: Any): Location {
    return PlatformFactory.getService<PlatformAdapter>().adaptLocation(any)
}

@Suppress("UNCHECKED_CAST")
fun <T> platformLocation(location: Location): T {
    return PlatformFactory.getService<PlatformAdapter>().platformLocation(location) as T
}

/**
 * 获取所有世界
 */
fun allWorlds(): List<String> {
    return PlatformFactory.getService<PlatformAdapter>().allWorlds()
}