package taboolib.platform

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.platform.type.VelocityCommandSender
import taboolib.platform.type.VelocityPlayer

/**
 * TabooLib
 * taboolib.platform.VelocityAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@PlatformSide([Platform.VELOCITY])
class VelocityAdapter : PlatformAdapter {

    val plugin by lazy { VelocityPlugin.getInstance() }

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(plugin.server.consoleCommandSource)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return plugin.server.allPlayers.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return VelocityPlayer(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is Player) adaptPlayer(any) else VelocityCommandSender(any as CommandSource)
    }
}