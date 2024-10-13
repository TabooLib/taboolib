package taboolib.platform.type

import net.afyer.afybroker.core.message.KickPlayerMessage
import net.afyer.afybroker.core.message.SendPlayerMessageMessage
import net.afyer.afybroker.core.message.SendPlayerTitleMessage
import net.afyer.afybroker.server.proxy.BrokerPlayer
import taboolib.common.platform.ProxyGameMode
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.warning
import taboolib.common.util.Location
import taboolib.common.util.Vector
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * TabooLib
 * taboolib.platform.type.AfyBrokerPlayer
 *
 * @author Ling556
 * @since 2024/5/09 23:51
 */
class AfyBrokerPlayer(val player: BrokerPlayer) : ProxyPlayer {

    override val origin: Any
        get() = player

    override val name: String
        get() = player.name

    override val address: InetSocketAddress
        get() = error("Unsupported")

    override val uniqueId: UUID
        get() = player.uniqueId

    override val ping: Int
        get() = error("Unsupported")

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
        val msg = KickPlayerMessage().setPlayer(player.name).setMessage(message)
        try {
            player.proxy.oneway(msg)
        } catch (ex: Exception) {
            warning(ex)
        }

    }

    override fun chat(message: String) {
        error("Unsupported")
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        error("Unsupported")
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        error("Unsupported")
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        val msg = SendPlayerTitleMessage()
                .setName(player.name)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setFadein(fadein)
                .setStay(stay).setFadeout(fadeout)
        try {
            player.server?.oneway(msg)
        } catch (ex: Exception) {
            warning(ex)
        }
    }

    override fun sendActionBar(message: String) {
        error("Unsupported")
    }

    override fun sendMessage(message: String) {
        val msg = SendPlayerMessageMessage().setUniqueId(player.uniqueId).setMessage(message)
        try {
            player.server?.oneway(msg)
        } catch (ex: Exception) {
            warning(ex)
        }
    }

    override fun sendRawMessage(message: String) {
        error("Unsupported")
    }

    override fun sendParticle(
        particle: ProxyParticle,
        location: Location,
        offset: Vector,
        count: Int,
        speed: Double,
        data: ProxyParticle.Data?
    ) {
        error("Unsupported")
    }

    override fun performCommand(command: String): Boolean {
        error("Unsupported")
    }

    override fun hasPermission(permission: String): Boolean {
        error("Unsupported")
    }

    override fun teleport(location: Location) {
        error("Unsupported")
    }

    override fun giveExp(exp: Int) {
        error("Unsupported")
    }

    override fun onQuit(callback: Runnable) {
        error("Unsupported")
    }
}