package taboolib.platform.type

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.platform.util.dispatchCommand
import taboolib.platform.util.toBukkitLocation
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.BukkitPlayer
 *
 * @author sky
 * @since 2021/6/17 10:33 下午
 */
class BukkitPlayer(val player: Player) : ProxyPlayer {

    override val origin: Any
        get() = player

    override val name: String
        get() = player.name

    override val address: InetSocketAddress?
        get() = player.address

    override val uniqueId: UUID
        get() = player.uniqueId

    override val ping: Int
        get() = player.ping

    override val locale: String
        get() = player.locale

    override val world: String
        get() = player.world.name

    override val location: Location
        get() = Location(world, player.location.x, player.location.y, player.location.z, player.location.yaw, player.location.pitch)

    override fun kick(message: String?) {
        player.kickPlayer(message)
    }

    override fun chat(message: String) {
        player.chat(message)
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(location.toBukkitLocation(), Sound.valueOf(sound), volume, pitch)
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(location.toBukkitLocation(), sound, volume, pitch)
    }

    override fun sendTitle(title: String, subtitle: String, fadein: Int, stay: Int, fadeout: Int) {
        player.sendTitle(title, subtitle, fadein, stay, fadeout)
    }

    override fun sendActionBar(message: String) {
        TODO("Not yet implemented")
    }

    override fun sendRawMessage(message: String) {
        player.sendRawMessage(message)
    }

    override fun sendMessage(message: String) {
        player.sendMessage(message)
    }

    override fun performCommand(command: String): Boolean {
        return dispatchCommand(player, command)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun teleport(loc: Location) {
        player.teleport(org.bukkit.Location(Bukkit.getWorld(loc.world!!), loc.x, loc.y, loc.z, loc.yaw, loc.pitch))
    }
}