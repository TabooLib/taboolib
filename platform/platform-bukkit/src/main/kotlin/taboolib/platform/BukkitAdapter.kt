package taboolib.platform

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import taboolib.platform.type.BukkitCommandSender
import taboolib.platform.type.BukkitPlayer
import taboolib.platform.util.toBukkitLocation
import taboolib.platform.util.toProxyLocation

/**
 * TabooLib
 * taboolib.platform.BukkitAdapter
 *
 * @author sky
 * @since 2021/6/17 12:22 上午
 */
@Awake
@PlatformSide([Platform.BUKKIT])
class BukkitAdapter : PlatformAdapter {

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(Bukkit.getConsoleSender())
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Bukkit.getWorlds().flatMap { it.players }.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return BukkitPlayer(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is Player) adaptPlayer(any) else BukkitCommandSender(any as CommandSender)
    }

    override fun adaptLocation(any: Any): Location {
        return (any as org.bukkit.Location).toProxyLocation()
    }

    override fun platformLocation(location: Location): Any {
        return location.toBukkitLocation()
    }

    override fun allWorlds(): List<String> {
        return Bukkit.getWorlds().map { it.name }
    }
}