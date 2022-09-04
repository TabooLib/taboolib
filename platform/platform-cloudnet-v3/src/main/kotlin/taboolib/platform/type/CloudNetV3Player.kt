package taboolib.platform.type

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
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.BungeePlayer
 *
 * @author CziSKY
 * @since 2021/6/21 13:41
 */
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
        get() = error("unsupported")

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
        player.playerExecutor.kick(message ?: "")
    }

    override fun chat(message: String) {
        player.playerExecutor.sendChatMessage(message)
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        error("unsupported")
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        error("unsupported")
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        error("unsupported")
    }

    override fun sendActionBar(message: String) {
        error("unsupported")
    }

    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun sendRawMessage(message: String) {
        AdventureComponentMessenger.sendMessage(player, GsonComponentSerializer.gson().deserialize(message))
    }

    override fun sendParticle(particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?) {
        error("unsupported")
    }

    override fun performCommand(command: String): Boolean {
        player.playerExecutor.dispatchProxyCommand(command)
        return true
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }

    override fun teleport(loc: Location) {
        error("unsupported")
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