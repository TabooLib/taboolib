package taboolib.platform

import de.dytanic.cloudnet.CloudNet
import de.dytanic.cloudnet.command.ICommandSender
import de.dytanic.cloudnet.common.document.gson.JsonDocument
import de.dytanic.cloudnet.ext.bridge.AdventureComponentMessenger
import de.dytanic.cloudnet.ext.bridge.BridgeServiceProperty
import de.dytanic.cloudnet.ext.bridge.player.CloudPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import taboolib.common.platform.ProxyGameMode
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.internal.Internal
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.BungeePlayer
 *
 * @author CziSKY
 * @since 2021/6/21 13:41
 */
@Internal
class CloudNetV3Player : ProxyPlayer {

    val sender: ICommandSender

    constructor(cloudPlayer: CloudPlayer): this(cloudPlayer.sender)

    constructor(sender: ICommandSender) {
        this.sender = sender
        this.player = sender.getProperty<CloudPlayer>("player")!!
        this.rawData =
            CloudNet.getInstance().cloudServiceProvider.getCloudService(player.connectedService.uniqueId).let {
                it ?: return@let JsonDocument()
                it.getProperty(BridgeServiceProperty.PLAYERS).orElse(null).find { it.name == this.name }?.rawData
                    ?: JsonDocument()
            }
    }


    private val player: CloudPlayer

    // 用于以后支持更多操作
    val rawData: JsonDocument

    override val origin: Any
        get() = sender

    override val name: String
        get() = player.name

    override val address: InetSocketAddress?
        get() = player.networkConnectionInfo.address.let { kotlin.runCatching { InetSocketAddress(it.host, it.port) }.getOrNull() }

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
        player.playerExecutor.kick(message ?: "")
    }

    override fun chat(message: String) {
        player.playerExecutor.sendChatMessage(message)
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        error("Unsupported")
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        error("Unsupported")
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        error("Unsupported")
    }

    override fun sendActionBar(message: String) {
        error("Unsupported")
    }

    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun sendRawMessage(message: String) {
        AdventureComponentMessenger.sendMessage(player, GsonComponentSerializer.gson().deserialize(message))
    }

    override fun sendParticle(particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?) {
        error("Unsupported")
    }

    override fun performCommand(command: String): Boolean {
        player.playerExecutor.dispatchProxyCommand(command)
        return true
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }

    override fun teleport(loc: Location) {
        error("Unsupported")
    }
}

class FilteredPlayer(@JvmField val player: CloudPlayer) : ICommandSender {

    override fun getName(): String = player.name

    override fun sendMessage(message: String) =
        AdventureComponentMessenger.sendMessage(player, Component.text(message))

    override fun sendMessage(vararg messages: String) =
        messages.forEach { sendMessage(it) }

    override fun hasPermission(permission: String) =
        CloudNet.getInstance().permissionManagement.getUser(player.uniqueId)?.hasPermission(permission)?.asBoolean() ?: false
}

val CloudPlayer.sender: ICommandSender get() = FilteredPlayer(this)

val FilteredPlayer.asPlayer get() = this.getProperty<CloudPlayer>("player")