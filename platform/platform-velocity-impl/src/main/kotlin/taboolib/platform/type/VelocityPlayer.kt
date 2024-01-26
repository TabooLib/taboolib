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
        get() = error("unsupported")

    override val world: String
        get() = error("unsupported")

    override val location: Location
        get() = error("unsupported")

    override var isOp: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
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

    override val sleepTicks: Int
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

    override var absorptionAmount: Double
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

    override var exp: Float
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
        error("unsupported")
    }

    override fun performCommand(command: String): Boolean {
        VelocityPlugin.getInstance().server.commandManager.executeAsync(player, command)
        return true
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun teleport(location: Location) {
        error("unsupported")
    }

    override fun giveExp(exp: Int) {
        error("unsupported")
    }
}