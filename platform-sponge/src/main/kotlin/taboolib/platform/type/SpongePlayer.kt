package taboolib.platform.type

import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.Sponge
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.EventContext
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.title.Title
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.SpongePlayer
 *
 * @author tr
 * @since 2021/6/21 15:49
 */
class SpongePlayer(val player: Player) : ProxyPlayer {

    override val origin: Any
        get() = player

    override val name: String
        get() = player.name

    override val address: InetSocketAddress?
        get() = player.connection.address

    override val uniqueId: UUID
        get() = player.uniqueId

    override val ping: Int
        get() = player.connection.latency

    override val locale: String
        get() = player.locale.displayName

    override val world: String
        get() = player.world.name

    override val location: Location
        get() = Location(world,
            player.location.x,
            player.location.y,
            player.location.z,
            player.headRotation.y.toFloat(),
            player.headRotation.x.toFloat())

    override fun kick(message: String?) {
        player.kick(Text.of(message ?: ""))
    }

    override fun chat(message: String) {
        player.simulateChat(Text.of(message), Cause.of(EventContext.empty(), ""))
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(SoundType.of(sound), Vector3d.from(location.x, location.y, location.z), volume.toDouble(), pitch.toDouble())
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        playSound(location, sound, volume, pitch)
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        player.sendTitle(Title.builder().title(Text.of(title ?: "")).subtitle(Text.of(subtitle ?: "")).fadeIn(fadein).stay(stay).fadeOut(fadeout).build())
    }

    override fun sendActionBar(message: String) {
        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(message))
    }

    override fun sendMessage(message: String) {
        player.sendMessage(Text.of(message))
    }

    // FixMe: 因为一些原因，我打算晚会再写。
    override fun sendRawMessage(message: String) {
        sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun performCommand(command: String): Boolean {
        return Sponge.getCommandManager().process(player, command).successCount.isPresent
    }

    override fun teleport(loc: Location) {
        val location =
            org.spongepowered.api.world.Location(Sponge.getServer().getWorld(loc.world ?: return).orElseThrow { error("The world must not be null!") },
                Vector3d.from(loc.yaw.toDouble(), loc.pitch.toDouble(), 0.0))
        player.location = location
    }
}