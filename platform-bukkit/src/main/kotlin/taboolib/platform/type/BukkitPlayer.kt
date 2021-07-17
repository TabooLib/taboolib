package taboolib.platform.type

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyGameMode
import taboolib.common.platform.ProxyPlayer
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.common.reflect.Reflex.Companion.reflexInvoke
import taboolib.common.util.Location
import taboolib.platform.util.dispatchCommand
import taboolib.platform.util.toBukkitLocation
import taboolib.platform.util.toProxyLocation
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.BukkitPlayer
 *
 * @author sky
 * @since 2021/6/17 10:33 下午
 */
class BukkitPlayer(val player: Player) : ProxyPlayer {

    val legacyVersion by lazy {
        Bukkit.getServer().javaClass.name.split('.')[3]
    }

    val rChatCompoundText by lazy {
        nmsClass("ChatComponentText").getDeclaredConstructor(String::class.java)
    }

    val rPacketPlayOutTitle by lazy {
        nmsClass("PacketPlayOutTitle").getDeclaredConstructor()
    }

    val rEnumTitleAction by lazy {
        nmsClass("PacketPlayOutTitle\$EnumTitleAction").enumConstants
    }

    fun nmsClass(name: String): Class<*> {
        return Class.forName("net.minecraft.server.$legacyVersion.$name")
    }

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
                player.reflex<Int>("entity/latency")!!
            } catch (ex: NoSuchFieldException) {
                player.reflex<Int>("entity/ping")!!
            }
        }

    override val locale: String
        get() = player.locale

    override val world: String
        get() = player.world.name

    override val location: Location
        get() = Location(world, player.location.x, player.location.y, player.location.z, player.location.yaw, player.location.pitch)

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
        get() = player.isSneaking

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

    override fun kick(message: String?) {
        player.kickPlayer(message)
    }

    override fun chat(message: String) {
        player.chat(message)
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(location.toBukkitLocation(), Sound.valueOf(sound), volume, pitch)
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(location.toBukkitLocation(), sound, volume, pitch)
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        try {
            player.sendTitle(title, subtitle, fadein, stay, fadeout)
        } catch (ex: NoSuchMethodError) {
            val connection = player.reflex<Any>("entity/playerConnection")!!
            if (title != null) {
                connection.reflexInvoke<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
                    it.reflex("a", rEnumTitleAction[0])
                    it.reflex("b", rChatCompoundText.newInstance(title))
                    it.reflex("c", fadein)
                    it.reflex("d", stay)
                    it.reflex("e", fadeout)
                })
            }
            if (subtitle != null) {
                connection.reflexInvoke<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
                    it.reflex("a", rEnumTitleAction[1])
                    it.reflex("b", rChatCompoundText.newInstance(subtitle))
                    it.reflex("c", fadein)
                    it.reflex("d", stay)
                    it.reflex("e", fadeout)
                })
            }
            connection.reflexInvoke<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
                it.reflex("a", rEnumTitleAction[3])
                it.reflex("c", fadein)
                it.reflex("d", stay)
                it.reflex("e", fadeout)
            })
        }
    }

    override fun sendActionBar(message: String) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message)[0])
    }

    override fun sendRawMessage(message: String) {
        player.spigot().sendMessage(*ComponentSerializer.parse(message))
    }

    override fun sendMessage(message: String) {
        player.sendMessage(message)
    }

    override fun performCommand(command: String): Boolean {
        return dispatchCommand(player, command)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun teleport(loc: Location) {
        player.teleport(org.bukkit.Location(Bukkit.getWorld(loc.world!!), loc.x, loc.y, loc.z, loc.yaw, loc.pitch))
    }
}