package taboolib.platform

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.command.CommandSender
import cn.nukkit.math.Vector3
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.platform.type.NukkitCommandSender
import taboolib.platform.type.NukkitPlayer
import taboolib.platform.util.toCommonLocation

/**
 * TabooLib
 * starslib.platform.NukkitAdapter
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

    override fun adaptLocation(any: Any): taboolib.common.util.Location {
        return (any as cn.nukkit.level.Location).toCommonLocation()
    }

    override fun platformLocation(location: taboolib.common.util.Location): Any {

        val level = NukkitPlugin.getInstance().server.getLevelByName(location.world)
        return cn.nukkit.level.Location.fromObject(Vector3(location.x, location.y, location.z), level, location.yaw.toDouble(), location.pitch.toDouble())
    }

    override fun allWorlds(): List<String> {
        return Server.getInstance().levels.values.map { it.name }
    }
}