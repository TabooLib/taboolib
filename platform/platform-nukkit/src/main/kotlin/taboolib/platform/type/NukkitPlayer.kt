package taboolib.platform.type

import cn.nukkit.AdventureSettings
import cn.nukkit.player.GameMode
import cn.nukkit.player.Player
import com.nukkitx.math.vector.Vector3f
import com.nukkitx.network.util.Preconditions
import com.nukkitx.protocol.bedrock.packet.PlaySoundPacket
import taboolib.common.platform.ProxyGameMode
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.platform.NukkitPlugin
import taboolib.platform.util.toCommonLocation
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
        get() = player.location.toCommonLocation()

    override var isOp: Boolean
        get() = player.isOp
        set(value) {
            player.isOp = value
        }

    override var compassTarget: Location
        get() = player.spawn.toCommonLocation()
        set(_) {
            error("unsupported")
        }

    override var bedSpawnLocation: Location?
        get() = player.spawn.toCommonLocation()
        set(_) {
            error("unsupported")
        }

    override var displayName: String?
        get() = player.displayName
        set(value) {
            player.displayName = value
        }

    override var playerListName: String?
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var gameMode: ProxyGameMode
        get() = ProxyGameMode.fromString(player.gamemode.name)
        set(value) {
            player.gamemode = GameMode.from(value.name)
        }

    override val isSneaking: Boolean
        get() = player.isSneaking

    override val isSprinting: Boolean
        get() = player.isSprinting

    override val isBlocking: Boolean
        get() = error("unsupported")

    override var isGliding: Boolean
        get() = player.isGliding
        set(value) {
            player.isGliding = value
        }

    override var isGlowing: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var isSwimming: Boolean
        get() = player.isSwimming
        set(value) {
            player.isSwimming = value
        }

    override val isRiptiding: Boolean
        get() = error("unsupported")

    override val isSleeping: Boolean
        get() = player.isSleeping

    override val sleepTicks: Int
        get() = error("unsupported")

    override var isSleepingIgnored: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val isDead: Boolean
        get() = player.health < 0

    override val isConversing: Boolean
        get() = error("unsupported")

    override val isLeashed: Boolean
        get() = error("unsupported")

    override val isOnGround: Boolean
        get() = player.isOnGround

    override val isInsideVehicle: Boolean
        get() = player.vehicle != null

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
        get() = player.firstPlayed

    override val lastPlayed: Long
        get() = player.lastPlayed

    override var absorptionAmount: Double
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var noDamageTicks: Int
        get() = player.noDamageTicks
        set(value) {
            player.setNoDamageTicks(value)
        }

    override var remainingAir: Int
        get() = player.airTicks
        set(value) {
            player.airTicks = value
        }

    override val maximumAir: Int
        get() = 400

    override var level: Int
        get() = player.experienceLevel
        set(value) {
            player.setExperience(player.experience, value)
        }

    override var exp: Float
        get() = player.experience.toFloat() / Player.calculateRequireExperience(player.experienceLevel)
        set(value) {
            Preconditions.checkArgument(value in 0f..1f)
            player.experience = (Player.calculateRequireExperience(player.experienceLevel) * value).toInt()
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
        get() = player.foodData.level
        set(value) {
            player.foodData.level = value
        }

    override var health: Double
        get() = player.health.toDouble()
        set(value) {
            player.health = value.toFloat()
        }

    override var maxHealth: Double
        get() = player.maxHealth.toDouble()
        set(value) {
            player.maxHealth = value.toInt()
        }

    override var allowFlight: Boolean
        get() = player.allowFlight
        set(value) {
            player.allowFlight = value
        }

    override var isFlying: Boolean
        get() = player.adventureSettings.get(AdventureSettings.Type.FLYING)
        set(value) {
            player.adventureSettings.set(AdventureSettings.Type.FLYING, value)
            player.adventureSettings.update()
        }

    override var flySpeed: Float
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var walkSpeed: Float
        get() = player.movementSpeed
        set(value) {
            player.movementSpeed = value
        }

    override val pose: String
        get() = error("unsupported")

    override val facing: String
        get() = player.horizontalFacing.name

    override fun isOnline(): Boolean {
        return player.isOnline
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

    override fun sendRawMessage(message: String) {
        sendMessage(message)
    }

    override fun sendParticle(particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?) {
        error("unsupported") // ParticleCommand::class.java
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun performCommand(command: String): Boolean {
        return NukkitCommandSender(player).performCommand(command)
    }

    override fun teleport(loc: Location) {
        val level = NukkitPlugin.getInstance().server.levelManager.getLevelByName(loc.world) ?: player.level
        val location = cn.nukkit.level.Location.from(loc.x.toFloat(), loc.y.toFloat(), loc.z.toFloat(), loc.yaw, loc.pitch, level)
        player.teleport(location)
    }
}