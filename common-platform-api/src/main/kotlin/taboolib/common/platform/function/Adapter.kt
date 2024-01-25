@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common.platform.function

import taboolib.common.Isolated
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import java.util.*

/**
 * 获取控制台
 */
inline fun console(): ProxyCommandSender {
    return PlatformFactory.getService<PlatformAdapter>().console()
}

/**
 * 将平台实现转换为跨平台实现
 */
inline fun adaptCommandSender(any: Any): ProxyCommandSender {
    return PlatformFactory.getService<PlatformAdapter>().adaptCommandSender(any)
}

/**
 * 获取所有在线玩家
 */
inline fun onlinePlayers(): List<ProxyPlayer> {
    return PlatformFactory.getService<PlatformAdapter>().onlinePlayers()
}

/**
 * 将平台实现转换为跨平台实现
 */
inline fun adaptPlayer(any: Any): ProxyPlayer {
    return PlatformFactory.getService<PlatformAdapter>().adaptPlayer(any)
}

/**
 * 通过名称获取玩家
 */
inline fun getProxyPlayer(name: String): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.name.equals(name, true) }
}

/**
 * 通过 UUID 获取玩家
 */
inline fun getProxyPlayer(uuid: UUID): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.uniqueId == uuid }
}

/**
 * 将平台实现转换为跨平台实现
 */
inline fun adaptLocation(any: Any): Location {
    return PlatformFactory.getService<PlatformAdapter>().adaptLocation(any)
}

/**
 * 将跨平台实现转换为平台实现
 */
@Suppress("UNCHECKED_CAST")
inline fun <T> platformLocation(location: Location): T {
    return PlatformFactory.getService<PlatformAdapter>().platformLocation(location) as T
}

/**
 * 获取所有世界
 */
inline fun allWorlds(): List<String> {
    return PlatformFactory.getService<PlatformAdapter>().allWorlds()
}