package taboolib.platform.type

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.title.Title
import taboolib.common.platform.ProxyGameMode
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.platform.VelocityPlugin
import java.net.InetSocketAddress
import java.time.Duration
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.VelocityPlayer
 *
 * @author CziSKY
 * @since 2021/6/21 13:41
 */
class VelocityPlayer(val player: Player) : ProxyPlayer {

    override val origin: Any
        get() = player

    override val name: String
        get() = player.username

    override val address: InetSocketAddress?
        get() = player.remoteAddress

    override val uniqueId: UUID
        get() = player.uniqueId

    override val ping: Int
        get() = player.ping.toInt()

    override val locale: String
        get() = error("Unsupported")

    override val world: String
        get() = error("Unsupported")

    override val location: Location
        get() = error("Unsupported")

    override var isOp: Boolean
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var compassTarget: Location
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var bedSpawnLocation: Location?
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var displayName: String?
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var playerListName: String?
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var gameMode: ProxyGameMode
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override val isSneaking: Boolean
        get() = error("Unsupported")

    override val isSprinting: Boolean
        get() = error("Unsupported")

    override val isBlocking: Boolean
        get() = error("Unsupported")

    override var isGliding: Boolean
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var isGlowing: Boolean
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var isSwimming: Boolean
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override val isRiptiding: Boolean
        get() = error("Unsupported")

    override val isSleeping: Boolean
        get() = error("Unsupported")

    override val sleepTicks: Int
        get() = error("Unsupported")

    override var isSleepingIgnored: Boolean
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override val isDead: Boolean
        get() = error("Unsupported")

    override val isConversing: Boolean
        get() = error("Unsupported")

    override val isLeashed: Boolean
        get() = error("Unsupported")

    override val isOnGround: Boolean
        get() = error("Unsupported")

    override val isInsideVehicle: Boolean
        get() = error("Unsupported")

    override var hasGravity: Boolean
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override val attackCooldown: Int
        get() = error("Unsupported")

    override var playerTime: Long
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override val firstPlayed: Long
        get() = error("Unsupported")

    override val lastPlayed: Long
        get() = error("Unsupported")

    override var absorptionAmount: Double
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var noDamageTicks: Int
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var remainingAir: Int
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override val maximumAir: Int
        get() = error("Unsupported")

    override var level: Int
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var exp: Float
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var exhaustion: Float
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var saturation: Float
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var foodLevel: Int
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var health: Double
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var maxHealth: Double
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var allowFlight: Boolean
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var isFlying: Boolean
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var flySpeed: Float
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var walkSpeed: Float
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override val pose: String
        get() = error("Unsupported")

    override val facing: String
        get() = error("Unsupported")

    override fun isOnline(): Boolean {
        return onlinePlayers().any { it.name == name }
    }

    override fun kick(message: String?) {
        player.disconnect(Component.text(message ?: ""))
    }

    override fun chat(message: String) {
        player.spoofChatInput(message)
    }

    // TODO: 2021/7/11 可能存在争议的写法
    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(Sound.sound(Key.key(sound), Sound.Source.MASTER, volume, pitch), location.x, location.y, location.z)
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        playSound(location, sound, volume, pitch)
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        player.showTitle(Title.title(
            Component.text(title ?: ""),
            Component.text(subtitle ?: ""),
            Title.Times.of(
                Duration.ofMillis(fadein * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeout * 50L)
            )
        ))
    }

    override fun sendActionBar(message: String) {
        player.sendActionBar(Component.text(message))
    }

    override fun sendMessage(message: String) {
        player.sendMessage(Component.text(message))
    }

    // 2021/7/6 Velocity Raw Message
    override fun sendRawMessage(message: String) {
        player.sendMessage(GsonComponentSerializer.gson().deserialize(message))
    }

    override fun sendParticle(particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?) {
        error("Unsupported")
    }

    override fun performCommand(command: String): Boolean {
        VelocityPlugin.getInstance().server.commandManager.executeAsync(player, command)
        return true
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun teleport(loc: Location) {
        error("Unsupported")
    }
}