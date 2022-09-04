package taboolib.platform.type

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.material.MaterialData
import taboolib.common.platform.ProxyGameMode
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.common.util.unsafeLazy
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
@Suppress("HasPlatformType")
class BukkitPlayer(val player: Player) : ProxyPlayer {

    val legacyVersion by unsafeLazy {
        Bukkit.getServer().javaClass.name.split('.')[3]
    }

    val rChatCompoundText by unsafeLazy {
        nmsClass("ChatComponentText").getDeclaredConstructor(String::class.java)
    }

    val rPacketPlayOutTitle by unsafeLazy {
        nmsClass("PacketPlayOutTitle").getDeclaredConstructor()
    }

    val rEnumTitleAction by unsafeLazy {
        nmsClass("PacketPlayOutTitle\$EnumTitleAction").enumConstants
    }

    val rPacketPlayOutChat by unsafeLazy {
        nmsClass("PacketPlayOutChat").getDeclaredConstructor()
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
        player.playSound(location.toBukkitLocation(), Sound.valueOf(sound), volume, pitch)
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(location.toBukkitLocation(), sound, volume, pitch)
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        try {
            player.sendTitle(title, subtitle, fadein, stay, fadeout)
        } catch (ex: NoSuchMethodError) {
            val connection = player.getProperty<Any>("entity/playerConnection")!!
            if (title != null) {
                connection.invokeMethod<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
                    it.setProperty("a", rEnumTitleAction[0])
                    it.setProperty("b", rChatCompoundText.newInstance(title))
                    it.setProperty("c", fadein)
                    it.setProperty("d", stay)
                    it.setProperty("e", fadeout)
                })
            }
            if (subtitle != null) {
                connection.invokeMethod<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
                    it.setProperty("a", rEnumTitleAction[1])
                    it.setProperty("b", rChatCompoundText.newInstance(subtitle))
                    it.setProperty("c", fadein)
                    it.setProperty("d", stay)
                    it.setProperty("e", fadeout)
                })
            }
            connection.invokeMethod<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
                it.setProperty("a", rEnumTitleAction[3])
                it.setProperty("c", fadein)
                it.setProperty("d", stay)
                it.setProperty("e", fadeout)
            })
        }
    }

    override fun sendActionBar(message: String) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(message))
        } catch (ex: NoSuchMethodError) {
            player.getProperty<Any>("entity/playerConnection")!!
                .invokeMethod<Void>("sendPacket", rPacketPlayOutChat.newInstance().also {
                    it.setProperty("b", 2.toByte())
                    it.setProperty("components", TextComponent.fromLegacyText(message))
                })
        }

    }

    override fun sendRawMessage(message: String) {
        player.spigot().sendMessage(*ComponentSerializer.parse(message))
    }

    override fun sendMessage(message: String) {
        player.sendMessage(message)
    }

    override fun sendParticle(particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?) {
        val bukkitParticle = try {
            Particle.valueOf(particle.name)
        } catch (ignored: IllegalArgumentException) {
            error("Unsupported particle ${particle.name}")
        }
        player.spawnParticle(
            bukkitParticle, location.toBukkitLocation(), count, offset.x, offset.y, offset.z, speed, when (data) {
                is ProxyParticle.DustTransitionData -> {
                    Particle.DustTransition(
                        Color.fromRGB(data.color.red, data.color.green, data.color.blue),
                        Color.fromRGB(data.toColor.red, data.toColor.blue, data.toColor.green),
                        data.size
                    )
                }
                is ProxyParticle.DustData -> {
                    Particle.DustOptions(Color.fromRGB(data.color.red, data.color.green, data.color.blue), data.size)
                }
                is ProxyParticle.ItemData -> {
                    val item = ItemStack(Material.valueOf(data.material))
                    val itemMeta = item.itemMeta!!
                    itemMeta.setDisplayName(data.name)
                    itemMeta.lore = data.lore
                    try {
                        itemMeta.setCustomModelData(data.customModelData)
                    } catch (ex: NoSuchMethodError) {
                    }
                    item.itemMeta = itemMeta
                    if (data.data != 0) {
                        item.durability = data.data.toShort()
                    }
                    item
                }
                is ProxyParticle.BlockData -> {
                    if (bukkitParticle.dataType == MaterialData::class.java) {
                        MaterialData(Material.valueOf(data.material), data.data.toByte())
                    } else {
                        Material.valueOf(data.material).createBlockData()
                    }
                }
                is ProxyParticle.VibrationData -> {
                    Vibration(
                        data.origin.toBukkitLocation(), when (val destination = data.destination) {
                            is ProxyParticle.VibrationData.LocationDestination -> {
                                Vibration.Destination.BlockDestination(destination.location.toBukkitLocation())
                            }
                            is ProxyParticle.VibrationData.EntityDestination -> {
                                Vibration.Destination.EntityDestination(Bukkit.getEntity(destination.entity)!!)
                            }
                            else -> error("out of case")
                        }, data.arrivalTime
                    )
                }
                else -> null
            }
        )
    }

    override fun performCommand(command: String): Boolean {
        return BukkitCommandSender(player).performCommand(command)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun teleport(loc: Location) {
        player.teleport(Location(Bukkit.getWorld(loc.world!!), loc.x, loc.y, loc.z, loc.yaw, loc.pitch))
    }
}