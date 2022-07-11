package taboolib.platform

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.entity.living.player.Player
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import taboolib.platform.type.Sponge7CommandSender
import taboolib.platform.type.Sponge7Player

/**
 * TabooLib
 * taboolib.platform.SpongeAdapter
 *
 * @author tr
 * @since 2021/6/21 17:02
 */
@Awake
@PlatformSide([Platform.SPONGE_API_7])
class Sponge7Adapter : PlatformAdapter {

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(Sponge.getServer().console)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Sponge.getServer().onlinePlayers.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return Sponge7Player(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is Player) adaptPlayer(any) else Sponge7CommandSender(any as CommandSource)
    }

    override fun adaptLocation(any: Any): Location {
        TODO("Not yet implemented")
    }

    override fun platformLocation(location: Location): Any {
        TODO("Not yet implemented")
    }

    override fun allWorlds(): List<String> {
        return Sponge.getServer().worlds.map { it.name }
    }
}