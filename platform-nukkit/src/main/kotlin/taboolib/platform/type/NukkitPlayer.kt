package taboolib.platform.type

import cn.nukkit.player.Player
import com.nukkitx.math.vector.Vector3f
import com.nukkitx.protocol.bedrock.packet.PlaySoundPacket
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.platform.NukkitPlugin
import taboolib.platform.util.dispatchCommand
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.NukkitPlayer
 *
 * @author CziSKY
 * @since 2021/6/20 0:01
 */
class NukkitPlayer(val player: Player) : ProxyPlayer {

    override val origin: Any
        get() = player

    override val name: String
        get() = player.name

    override val address: InetSocketAddress?
        get() = player.socketAddress

    override val uniqueId: UUID
        get() = NukkitPlugin.getInstance().server.onlinePlayers.filter { it.value == player }.keys.first()

    override val ping: Int
        get() = player.ping.toInt()

    override val locale: String
        get() = player.locale.displayName

    override val world: String
        get() = player.level.name

    override val location: Location
        get() {
            val loc = player.location
            return Location(world, loc.x.toDouble(), loc.y.toDouble(), loc.z.toDouble(), loc.yaw, loc.pitch)
        }

    override var isOp: Boolean
        get() = player.isOp
        set(value) {
            player.isOp = value
        }

    override fun kick(message: String?) {
        player.kick(message)
    }

    override fun chat(message: String) {
        player.chat(message)
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        player.level.addChunkPacket(player.chunk.x, player.chunk.z, PlaySoundPacket().also { packet ->
            packet.position = Vector3f.from(location.x, location.y, location.z)
            packet.sound = sound
            packet.volume = volume
            packet.pitch = pitch
        })
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        playSound(location, sound, volume, pitch)
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        player.sendTitle(title ?: "", subtitle ?: "", fadein, stay, fadeout)
    }

    override fun sendActionBar(message: String) {
        player.sendActionBar(message)
    }

    override fun sendMessage(message: String) {
        player.sendMessage(message)
    }

    // 暂时还未找到 Nukkit 如何发 Json 信息的方法...
    // 所以先这样写, 晚会再找一下实现方法。
    override fun sendRawMessage(message: String) {
        sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun performCommand(command: String): Boolean {
        return dispatchCommand(player, command)
    }

    override fun teleport(loc: Location) {
        val level = NukkitPlugin.getInstance().server.levelManager.getLevelByName(loc.world) ?: player.level
        player.teleport(cn.nukkit.level.Location.from(loc.x.toFloat(), loc.y.toFloat(), loc.z.toFloat(), loc.yaw, loc.pitch, level))
    }
}