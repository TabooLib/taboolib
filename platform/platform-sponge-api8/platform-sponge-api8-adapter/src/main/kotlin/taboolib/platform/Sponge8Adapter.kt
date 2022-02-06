package taboolib.platform

import net.kyori.adventure.audience.Audience
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.server.ServerPlayer
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import taboolib.internal.Internal

/**
 * TabooLib
 * taboolib.platform.SpongeAdapter
 *
 * @author tr
 * @since 2021/6/21 17:02
 */
@Internal
@Awake
@PlatformSide([Platform.SPONGE_API_8])
class Sponge8Adapter : PlatformAdapter {

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(Sponge.systemSubject())
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Sponge.server().onlinePlayers().map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return Sponge8Player(any as ServerPlayer)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is ServerPlayer) adaptPlayer(any) else Sponge8CommandSender(any as Audience)
    }

    override fun adaptLocation(any: Any): Location {
        error("Unsupported")
    }

    override fun platformLocation(location: Location): Any {
        error("Unsupported")
    }
}