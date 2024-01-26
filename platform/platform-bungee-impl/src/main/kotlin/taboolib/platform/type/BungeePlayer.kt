package taboolib.platform.type

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.chat.ComponentSerializer
import taboolib.common.platform.ProxyGameMode
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.platform.BungeePlugin
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.BungeePlayer
 *
 * @author CziSKY
 * @since 2021/6/21 13:41
 */
class BungeePlayer(val player: ProxiedPlayer) : ProxyPlayer {

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
        get() = player.locale.displayName

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
        player.disconnect(TextComponent(message))
    }

    override fun chat(message: String) {
        player.chat(message)
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        error("unsupported")
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        error("unsupported")
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        val titleMessage = BungeePlugin.getInstance().proxy.createTitle().also {
            it.title(TextComponent(title ?: ""))
            it.subTitle(TextComponent(title ?: ""))
            it.fadeIn(fadein)
            it.stay(stay)
            it.fadeOut(fadeout)
        }
        titleMessage.send(player)
    }

    override fun sendActionBar(message: String) {
        player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent(message))
    }

    override fun sendMessage(message: String) {
        player.sendMessage(TextComponent(message))
    }

    override fun sendRawMessage(message: String) {
        player.sendMessage(TextComponent(*ComponentSerializer.parse(message)))
    }

    override fun sendParticle(particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?) {
        error("unsupported")
    }

    override fun performCommand(command: String): Boolean {
        return BungeePlugin.getInstance().proxy.pluginManager.dispatchCommand(player, command)
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