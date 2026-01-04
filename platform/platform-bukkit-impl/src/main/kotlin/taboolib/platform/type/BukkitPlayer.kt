@file:Suppress("DEPRECATION", "LocalVariableName")

package taboolib.platform.type

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.platform.ProxyGameMode
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.common.util.orNull
import taboolib.library.xseries.particles.XParticle
import taboolib.library.xseries.XSound
import taboolib.library.xseries.base.XBase
import taboolib.platform.util.LegacyPlayer
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

/**
 * TabooLib
 * taboolib.platform.type.BukkitPlayer
 *
 * @author sky
 * @since 2021/6/17 10:33 下午
 */
class BukkitPlayer(val player: Player) : ProxyPlayer {

    override val origin: Any
        get() = player

    override val name: String
        get() = player.name

    override val address: InetSocketAddress?
        get() = player.address

    override val uniqueId: UUID
        get() = player.uniqueId

    override val ping: Int
        get() {
            return try {
                player.getProperty<Int>("entity/latency")!!
            } catch (ex: NoSuchFieldException) {
                player.getProperty<Int>("entity/ping")!!
            }
        }

    override val locale: String
        get() = try {
            player.locale
        } catch (ignored: NoSuchMethodError) {
            player.getProperty<String>("entity/locale")!!
        }

    override val world: String
        get() = player.world.name

    override val location: Location
        get() = Location(
            world, player.location.x, player.location.y, player.location.z, player.location.yaw, player.location.pitch
        )

    override var compassTarget: Location
        get() = player.compassTarget.toProxyLocation()
        set(value) {
            player.compassTarget = value.toBukkitLocation()
        }

    override var bedSpawnLocation: Location?
        get() = player.bedSpawnLocation?.toProxyLocation()
        set(value) {
            player.bedSpawnLocation = value!!.toBukkitLocation()
        }

    override var displayName: String?
        get() = player.displayName
        set(value) {
            player.setDisplayName(value)
        }

    override var playerListName: String?
        get() = player.playerListName
        set(value) {
            player.setPlayerListName(value)
        }

    override var gameMode: ProxyGameMode
        get() = ProxyGameMode.fromString(player.gameMode.name)
        set(value) {
            player.gameMode = GameMode.valueOf(value.name.uppercase())
        }

    override val isSneaking: Boolean
        get() = player.isSneaking

    override val isSprinting: Boolean
        get() = player.isSprinting

    override val isBlocking: Boolean
        get() = player.isBlocking

    override var isGliding: Boolean
        get() = player.isGliding
        set(value) {
            player.isGliding = value
        }

    override var isGlowing: Boolean
        get() = player.isGlowing
        set(value) {
            player.isGlowing = value
        }

    override var isSwimming: Boolean
        get() = player.isSwimming
        set(value) {
            player.isSwimming = value
        }

    override val isRiptiding: Boolean
        get() = player.isRiptiding

    override val isSleeping: Boolean
        get() = player.isSleeping

    override val sleepTicks: Int
        get() = player.sleepTicks

    override var isSleepingIgnored: Boolean
        get() = player.isSleepingIgnored
        set(value) {
            player.isSleepingIgnored = value
        }

    override val isDead: Boolean
        get() = player.isDead

    override val isConversing: Boolean
        get() = player.isConversing

    override val isLeashed: Boolean
        get() = player.isLeashed

    override val isOnGround: Boolean
        get() = player.isOnGround

    override val isInsideVehicle: Boolean
        get() = player.isInsideVehicle

    override var hasGravity: Boolean
        get() = player.hasGravity()
        set(value) {
            player.setGravity(value)
        }

    override val attackCooldown: Int
        get() = player.attackCooldown.toInt()

    override var playerTime: Long
        get() = player.playerTime
        set(value) {
            player.setPlayerTime(value, true)
        }

    override val firstPlayed: Long
        get() = player.firstPlayed

    override val lastPlayed: Long
        get() = player.lastPlayed

    override var absorptionAmount: Double
        get() = player.absorptionAmount
        set(value) {
            player.absorptionAmount = value
        }

    override var noDamageTicks: Int
        get() = player.noDamageTicks
        set(value) {
            player.noDamageTicks = value
        }

    override var remainingAir: Int
        get() = player.remainingAir
        set(value) {
            player.remainingAir = value
        }

    override val maximumAir: Int
        get() = player.maximumAir

    override var level: Int
        get() = player.level
        set(value) {
            player.level = value
        }

    override var exp: Float
        get() = player.exp
        set(value) {
            player.exp = value
        }

    override var exhaustion: Float
        get() = player.exhaustion
        set(value) {
            player.exhaustion = value
        }

    override var saturation: Float
        get() = player.saturation
        set(value) {
            player.saturation = value
        }

    override var foodLevel: Int
        get() = player.foodLevel
        set(value) {
            player.foodLevel = value
        }

    override var health: Double
        get() = player.health
        set(value) {
            player.health = value
        }

    override var maxHealth: Double
        get() = player.maxHealth
        set(value) {
            player.maxHealth = value
        }

    override var allowFlight: Boolean
        get() = player.allowFlight
        set(value) {
            player.allowFlight = value
        }

    override var isFlying: Boolean
        get() = player.isFlying
        set(value) {
            player.isFlying = value
        }

    override var flySpeed: Float
        get() = player.flySpeed
        set(value) {
            player.flySpeed = value
        }

    override var walkSpeed: Float
        get() = player.walkSpeed
        set(value) {
            player.walkSpeed = value
        }

    override val pose: String
        get() = player.pose.name

    override val facing: String
        get() = player.facing.name

    override var isOp: Boolean
        get() = player.isOp
        set(value) {
            player.isOp = value
        }

    override fun isOnline(): Boolean {
        return player.isOnline
    }

    override fun kick(message: String?) {
        player.kickPlayer(message)
    }

    override fun chat(message: String) {
        player.chat(message)
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        if (volume == -1f && pitch == -1f) {
            player.stopSound(sound)
            return
        }
        XSound.of(sound).ifPresent { player.playSound(location.toBukkitLocation(), it.get()!!, volume, pitch) }
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        if (volume == -1f && pitch == -1f) {
            player.stopSound(sound)
            return
        }
        player.playSound(location.toBukkitLocation(), sound, volume, pitch)
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        try {
            player.sendTitle(title, subtitle, fadein, stay, fadeout)
        } catch (ex: NoSuchMethodError) {
            LegacyPlayer.sendTitle(player, title, subtitle, fadein, stay, fadeout)
        }
    }

    override fun sendActionBar(message: String) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(message))
        } catch (ex: NoSuchMethodError) {
            LegacyPlayer.sendActionBar(player, message)
        }
    }

    override fun sendRawMessage(message: String) {
        player.spigot().sendMessage(*ComponentSerializer.parse(message))
    }

    override fun sendMessage(message: String) {
        player.sendMessage(message)
    }

    override fun sendParticle(particle: String, location: Location, offset: Vector, count: Int, speed: Double, data: Any?) {
        val xbase = XParticle.of(particle).orNull() as? XBase<*, Particle> ?: return
        player.spawnParticle(xbase.get()!!, location.toBukkitLocation(), count, offset.x, offset.y, offset.z, speed, data)
    }

    override fun performCommand(command: String): Boolean {
        return BukkitCommandSender(player).performCommand(command)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun teleport(location: Location) {
        player.teleport(Location(Bukkit.getWorld(location.world!!), location.x, location.y, location.z, location.yaw, location.pitch))
    }

    override fun giveExp(exp: Int) {
        player.giveExp(exp)
    }

    val quitCallback = CopyOnWriteArraySet<Runnable>()

    override fun onQuit(callback: Runnable) {
        quitCallback += callback
    }

    fun Location.toBukkitLocation(): org.bukkit.Location {
        return Location(world?.let { Bukkit.getWorld(it) }, x, y, z, yaw, pitch)
    }

    fun org.bukkit.Location.toProxyLocation(): Location {
        return Location(world?.name, x, y, z, yaw, pitch)
    }

    companion object {

        @SubscribeEvent
        private fun onQuit(e: PlayerQuitEvent) {
            BukkitPlayer(e.player).quitCallback.forEach(Runnable::run)
        }
    }
}
