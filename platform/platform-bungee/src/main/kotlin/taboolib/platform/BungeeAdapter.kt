package taboolib.platform

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import taboolib.common.util.unsafeLazy
import taboolib.platform.type.BungeeCommandSender
import taboolib.platform.type.BungeePlayer

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@PlatformSide([Platform.BUNGEE])
class BungeeAdapter : PlatformAdapter {

    val plugin by unsafeLazy { BungeePlugin.getInstance() }

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(plugin.proxy.console)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return plugin.proxy.players.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return BungeePlayer(any as ProxiedPlayer)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is ProxiedPlayer) adaptPlayer(any) else BungeeCommandSender(any as CommandSender)
    }

    override fun adaptLocation(any: Any): Location {
        error("unsupported")
    }

    override fun platformLocation(location: Location): Any {
        error("unsupported")
    }

    override fun allWorlds(): List<String> {
        TODO("Not yet implemented")
    }
}