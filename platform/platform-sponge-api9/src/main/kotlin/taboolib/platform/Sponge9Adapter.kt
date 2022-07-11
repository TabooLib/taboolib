package taboolib.platform

import net.kyori.adventure.audience.Audience
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.server.ServerPlayer
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.platform.type.Sponge9CommandSender
import taboolib.platform.type.Sponge9Player

/**
 * TabooLib
 * taboolib.platform.SpongeAdapter
 *
 * @author tr
 * @since 2021/6/21 17:02
 */
@Awake
@PlatformSide([Platform.SPONGE_API_9])
class Sponge9Adapter : PlatformAdapter {

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(Sponge.systemSubject())
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Sponge.server().onlinePlayers().map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return Sponge9Player(any as ServerPlayer)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is ServerPlayer) adaptPlayer(any) else Sponge9CommandSender(any as Audience)
    }

    override fun adaptLocation(any: Any): Location {
        TODO("Not yet implemented")
    }

    override fun platformLocation(location: Location): Any {
        TODO("Not yet implemented")
    }

    override fun allWorlds(): List<String> {
        TODO("Not yet implemented")
    }
}