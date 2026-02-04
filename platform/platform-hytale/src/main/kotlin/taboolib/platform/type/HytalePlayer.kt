package taboolib.platform.type

import com.hypixel.hytale.protocol.GameMode
import com.hypixel.hytale.protocol.packets.connection.PongType
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandManager
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.permissions.PermissionsModule
import taboolib.common.platform.ProxyGameMode
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.HytalePlayer
 *
 * @author sky
 * @since 2024/1/1
 */
@Suppress("removal")
class HytalePlayer(val player: Player) : ProxyPlayer {

    override val origin: Any
        get() = player

    override val name: String
        get() = player.displayName

    override val address: InetSocketAddress?
        get() = player.playerRef.packetHandler.channel.remoteAddress() as? InetSocketAddress

    override val uniqueId: UUID
        get() = player.uuid ?: error("Player UUID is null")

    override val ping: Int
        get() {
            val pingInfo = player.playerRef.packetHandler.getPingInfo(PongType.Tick)
            // Ping is stored in microseconds, convert to milliseconds
            return (pingInfo.pingMetricSet.lastValue / 1000).toInt()
        }

    override val locale: String
        get() = player.playerRef.language

    override val world: String
        get() = player.world?.name ?: ""

    override val location: Location
        get() {
            val transform = player.playerRef.transform
            val position = transform.position
            val rotation = transform.rotation
            return Location(
                player.world?.name ?: "",
                position.x,
                position.y,
                position.z,
                rotation.yaw,
                rotation.pitch
            )
        }

    override var isOp: Boolean
        get() {
            val uuid = player.uuid ?: return false
            return PermissionsModule.get().getGroupsForUser(uuid).contains("OP")
        }
        set(value) {
            val uuid = player.uuid ?: return
            if (value) {
                PermissionsModule.get().addUserToGroup(uuid, "OP")
            } else {
                PermissionsModule.get().removeUserFromGroup(uuid, "OP")
            }
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
        get() = player.displayName
        set(_) {
            error("Unsupported")
        }

    override var playerListName: String?
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override var gameMode: ProxyGameMode
        get() = when (player.gameMode) {
            GameMode.Creative -> ProxyGameMode.CREATIVE
            GameMode.Adventure -> ProxyGameMode.ADVENTURE
            else -> ProxyGameMode.SURVIVAL // Default fallback
        }
        set(_) {
            error("Unsupported") // GameMode setting requires ECS access
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
        return player.playerRef.packetHandler.stillActive()
    }

    override fun kick(message: String?) {
        player.playerRef.packetHandler.disconnect(message ?: "Kicked")
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
        error("Unsupported")
    }

    override fun sendActionBar(message: String) {
        error("Unsupported")
    }

    override fun sendMessage(message: String) {
        player.sendMessage(Message.raw(HytaleCommandSender.stripColor(message)))
    }

    override fun sendRawMessage(message: String) {
        player.sendMessage(Message.raw(HytaleCommandSender.stripColor(message)))
    }

    override fun sendParticle(particle: String, location: Location, offset: Vector, count: Int, speed: Double, data: Any?) {
        error("Unsupported")
    }

    override fun performCommand(command: String): Boolean {
        // 使用 CommandManager 执行命令
        val future = CommandManager.get().handleCommand(player, command)
        return try {
            future.get() // 等待命令执行完成
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun teleport(location: Location) {
        error("Unsupported")
    }

    override fun giveExp(exp: Int) {
        error("Unsupported")
    }

    override fun onQuit(callback: Runnable) {
        // TODO: 实现退出回调
    }
}
