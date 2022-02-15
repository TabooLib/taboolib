package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import java.util.*

fun console(): ProxyCommandSender =
    PlatformFactory.getPlatformService<PlatformAdapter>().console()

fun adaptCommandSender(any: Any): ProxyCommandSender =
    PlatformFactory.getPlatformService<PlatformAdapter>().adaptCommandSender(any)

fun onlinePlayers(): List<ProxyPlayer> =
    PlatformFactory.getPlatformService<PlatformAdapter>().onlinePlayers()

fun adaptPlayer(any: Any): ProxyPlayer =
    PlatformFactory.getPlatformService<PlatformAdapter>().adaptPlayer(any)

fun getProxyPlayer(name: String): ProxyPlayer? =
    onlinePlayers().firstOrNull { it.name == name }

fun getProxyPlayer(uuid: UUID): ProxyPlayer? =
    onlinePlayers().firstOrNull { it.uniqueId == uuid }

fun adaptLocation(any: Any): Location =
    PlatformFactory.getPlatformService<PlatformAdapter>().adaptLocation(any)

@Suppress("UNCHECKED_CAST")
fun <T> platformLocation(location: Location): T =
    PlatformFactory.getPlatformService<PlatformAdapter>().platformLocation(location) as T
