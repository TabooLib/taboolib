package taboolib.platform.type

import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.Game
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.Property
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.EventContext
import org.spongepowered.api.service.permission.SubjectData
import org.spongepowered.api.service.whitelist.WhitelistService
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.title.Title
import org.spongepowered.api.util.Tristate
import taboolib.common.platform.ProxyGameMode
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
        get() {
            val loc = player.location
            return Location(world, loc.x, loc.y, loc.z, player.headRotation.y.toFloat(), player.headRotation.x.toFloat())
        }

    override var isOp: Boolean
        get() = player.hasPermission("*")
        set(value) {
            player.subjectData.setPermission(SubjectData.GLOBAL_CONTEXT, "*", if (value) Tristate.TRUE else Tristate.UNDEFINED)
        }

    override var compassTarget: Location
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var bedSpawnLocation: Location?
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var displayName: String?
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var playerListName: String?
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var gameMode: ProxyGameMode
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val isSneaking: Boolean
        get() = error("unsupported")

    override val isSprinting: Boolean
        get() = error("unsupported")

    override val isBlocking: Boolean
        get() = error("unsupported")

    override var isGliding: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var isGlowing: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var isSwimming: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val isRiptiding: Boolean
        get() = error("unsupported")

    override val isSleeping: Boolean
        get() = error("unsupported")

    override val sleepTicks: Boolean
        get() = error("unsupported")

    override var isSleepingIgnored: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val isDead: Boolean
        get() = error("unsupported")

    override val isConversing: Boolean
        get() = error("unsupported")

    override val isLeashed: Boolean
        get() = error("unsupported")

    override val isOnGround: Boolean
        get() = error("unsupported")

    override val isInsideVehicle: Boolean
        get() = error("unsupported")

    override var isJumping: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var hasGravity: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val attackCooldown: Int
        get() = error("unsupported")

    override var playerTime: Long
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val firstPlayed: Long
        get() = error("unsupported")

    override val lastPlayed: Long
        get() = error("unsupported")

    override val lastLogin: Long
        get() = error("unsupported")

    override val lastSeen: Long
        get() = error("unsupported")

    override var absorptionAmount: Int
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var noDamageTicks: Int
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var remainingAir: Int
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val maximumAir: Int
        get() = error("unsupported")

    override var level: Int
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var exp: Int
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var exhaustion: Float
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var saturation: Float
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var foodLevel: Int
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var health: Double
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var maxHealth: Double
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var allowFlight: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var isFlying: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var flySpeed: Float
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var walkSpeed: Float
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val pose: String
        get() = error("unsupported")

    override val facing: String
        get() = error("unsupported")

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
        val world = Sponge.getServer().getWorld(loc.world ?: return).orElseThrow()
        val location = org.spongepowered.api.world.Location(world, Vector3d.from(loc.x, loc.y, loc.z))
        player.location = location
        player.headRotation = Vector3d.from(loc.yaw.toDouble(), loc.pitch.toDouble(), 0.0)
    }
}