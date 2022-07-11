package taboolib.platform

import cn.nukkit.Server
import cn.nukkit.command.CommandSender
import cn.nukkit.player.Player
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import taboolib.platform.type.NukkitCommandSender
import taboolib.platform.type.NukkitPlayer
import taboolib.platform.util.toCommonLocation

/**
 * TabooLib
 * taboolib.platform.NukkitAdapter
 *
 * @author CziSKY
 * @since 2021/6/20 0:46
 */
@Awake
@PlatformSide([Platform.NUKKIT])
class NukkitAdapter : PlatformAdapter {

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(Server.getInstance().consoleSender)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Server.getInstance().onlinePlayers.values.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return NukkitPlayer(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is Player) adaptPlayer(any) else NukkitCommandSender(any as CommandSender)
    }

    override fun adaptLocation(any: Any): Location {
        return (any as cn.nukkit.level.Location).toCommonLocation()
    }

    override fun platformLocation(location: Location): Any {
        val level = NukkitPlugin.getInstance().server.levelManager.getLevelByName(location.world)
        return cn.nukkit.level.Location.from(location.x.toFloat(), location.y.toFloat(), location.z.toFloat(), location.yaw, location.pitch, level)
    }

    override fun allWorlds(): List<String> {
        return Server.getInstance().levels.map { it.name }
    }
}