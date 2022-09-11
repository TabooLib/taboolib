package taboolib.platform.type

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import org.spongepowered.api.ResourceKey
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.Keys
import org.spongepowered.api.entity.living.player.gamemode.GameModes
import org.spongepowered.api.entity.living.player.server.ServerPlayer
import org.spongepowered.api.event.Cause
import org.spongepowered.api.event.EventContext
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.service.permission.SubjectData
import org.spongepowered.api.util.Direction
import org.spongepowered.api.util.Ticks
import org.spongepowered.api.util.Tristate
import org.spongepowered.api.world.server.ServerLocation
import org.spongepowered.math.vector.Vector3d
import taboolib.common.platform.ProxyGameMode
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.platform.util.toPlain
import java.net.InetSocketAddress
import java.time.Duration
import java.util.*


/**
 * TabooLib
 * taboolib.platform.type.SpongePlayer
 *
 * @author tr
 * @since 2021/6/21 15:49
 */
class Sponge8Player(val player: ServerPlayer) : ProxyPlayer {

    override val origin: Any
        get() = player

    override val name: String
        get() = player.name()

    override val address: InetSocketAddress?
        get() = player.connection().address()

    override val uniqueId: UUID
        get() = player.uniqueId()

    override val ping: Int
        get() = player.connection().latency()

    override val locale: String
        get() = player.locale().displayName

    override val world: String
        get() = PlainTextComponentSerializer.plainText().serialize(player.world().properties().displayName().get())

    override val location: Location
        get() {
            val loc = player.location()
            return Location(world, loc.x(), loc.y(), loc.z(), player.headRotation().get().y().toFloat(), player.headRotation().get().x().toFloat())
        }

    override var isOp: Boolean
        get() = player.hasPermission("*")
        set(value) {
            player.subjectData().setPermission(SubjectData.GLOBAL_CONTEXT, "*", if (value) Tristate.TRUE else Tristate.UNDEFINED)
        }

    override var compassTarget: Location
        get() {
            val vector3d = player.get(Keys.TARGET_LOCATION).get()
            return Location(world, vector3d.x(), vector3d.y(), vector3d.z())
        }
        set(value) {
            player.offer(Keys.TARGET_LOCATION, Vector3d(value.x, value.y, value.z))
        }

    override var bedSpawnLocation: Location?
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var displayName: String?
        get() = PlainTextComponentSerializer.plainText().serialize(player.displayName().get() ?: Component.text(player.name()))
        set(value) {
            player.displayName().set(Component.text(value ?: player.name() ?: ""))
        }

    override var playerListName: String?
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var gameMode: ProxyGameMode
        get() = ProxyGameMode.fromString(player.gameMode().get().asComponent().toPlain())
        set(value) {
            player.gameMode().set(GameModes::class.java.getProperty(value.name, isStatic = true)!!)
        }

    override val isSneaking: Boolean
        get() = player.get(Keys.IS_SNEAKING).get()

    override val isSprinting: Boolean
        get() = player.get(Keys.IS_SPRINTING).get()

    override val isBlocking: Boolean
        get() {
            val item = player.activeItem().get()
            return !item.isEmpty && item.type() == ItemTypes.SHIELD.get()
        }

    override var isGliding: Boolean
        get() = player.get(Keys.IS_ELYTRA_FLYING).get()
        set(value) {
            player.offer(Keys.IS_ELYTRA_FLYING, value)
        }

    override var isGlowing: Boolean
        get() = player.get(Keys.IS_GLOWING).get()
        set(value) {
            player.offer(Keys.IS_GLOWING, value)
        }

    override var isSwimming: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val isRiptiding: Boolean
        get() = error("unsupported")

    override val isSleeping: Boolean
        get() = player.get(Keys.IS_SLEEPING).get()

    override val sleepTicks: Int
        get() = player.get(Keys.SLEEP_TIMER).get()

    override var isSleepingIgnored: Boolean
        get() = player.sleepingIgnored().get()
        set(value) {
            player.offer(Keys.IS_SLEEPING_IGNORED, value)
        }

    override val isDead: Boolean
        get() = player.health().get() <= 0

    override val isConversing: Boolean
        get() = error("unsupported")

    override val isLeashed: Boolean
        get() = error("unsupported")

    override val isOnGround: Boolean
        get() = player.onGround().get()

    override val isInsideVehicle: Boolean
        get() = player.vehicle().isPresent

    override var hasGravity: Boolean
        get() = player.gravityAffected().get()
        set(value) {
            player.offer(Keys.IS_GRAVITY_AFFECTED, value)
        }

    override val attackCooldown: Int
        get() = error("unsupported")

    override var playerTime: Long
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val firstPlayed: Long
        get() = player.firstJoined().get().get().toEpochMilli()

    override val lastPlayed: Long
        get() = player.lastPlayed().get().get().toEpochMilli()

    override var absorptionAmount: Double
        get() = player.get(Keys.ABSORPTION).orElse(0.0)
        set(value) {
            player.offer(Keys.ABSORPTION, value)
        }

    override var noDamageTicks: Int
        get() = player.get(Keys.INVULNERABILITY_TICKS).orElse(Ticks.of(0)).ticks().toInt()
        set(value) {
            player.offer(Keys.INVULNERABILITY_TICKS, Ticks.of(value.toLong()))
        }

    override var remainingAir: Int
        get() = player.get(Keys.REMAINING_AIR).orElse(0)
        set(value) {
            player.offer(Keys.REMAINING_AIR, value)
        }

    override val maximumAir: Int
        get() = player.get(Keys.MAX_AIR).orElse(0)

    override var level: Int
        get() = player.get(Keys.EXPERIENCE_LEVEL).get()
        set(value) {
            player.offer(Keys.EXPERIENCE_LEVEL, value)
        }

    override var exp: Float
        get() = player.get(Keys.EXPERIENCE_SINCE_LEVEL).get().toFloat()
        set(value) {
            player.offer(Keys.EXPERIENCE_SINCE_LEVEL, value.toInt())
        }

    override var exhaustion: Float
        get() = player.exhaustion().get().toFloat()
        set(value) {
            player.offer(Keys.EXHAUSTION, value.toDouble())
        }

    override var saturation: Float
        get() = player.saturation().get().toFloat()
        set(value) {
            player.offer(Keys.SATURATION, value.toDouble())
        }

    override var foodLevel: Int
        get() = player.foodLevel().get()
        set(value) {
            player.offer(Keys.FOOD_LEVEL, value)
        }

    override var health: Double
        get() = player.health().get()
        set(value) {
            player.offer(Keys.HEALTH, value)
        }

    override var maxHealth: Double
        get() = player.maxHealth().get()
        set(value) {
            player.offer(Keys.MAX_HEALTH, value)
        }

    override var allowFlight: Boolean
        get() = player.get(Keys.CAN_FLY).get()
        set(it) {
            player.offer(Keys.CAN_FLY, it)
        }

    override var isFlying: Boolean
        get() = player.get(Keys.IS_FLYING).get()
        set(it) {
            player.offer(Keys.IS_FLYING, it)
        }

    override var flySpeed: Float
        get() = player.get(Keys.FLYING_SPEED).get().toFloat()
        set(it) {
            player.offer(Keys.FLYING_SPEED, it.toDouble())
        }

    override var walkSpeed: Float
        get() = player.get(Keys.WALKING_SPEED).get().toFloat()
        set(it) {
            player.offer(Keys.WALKING_SPEED, it.toDouble())
        }

    override val pose: String
        get() {
            // 忽略：SPIN_ATTACK, LONG_JUMPING
            return when {
                isDead -> "DYING"
                isGliding -> "FALL_FLYING"
                isSleeping -> "SLEEPING"
                isSneaking -> "SNEAKING"
                else -> "STANDING"
            }
        }

    override val facing: String
        get() = Direction.closest(player.transform().rotation()).name

    override fun isOnline(): Boolean {
        return player.isOnline
    }

    override fun kick(message: String?) {
        player.kick(Component.text(message ?: ""))
    }

    override fun chat(message: String) {
        player.simulateChat(Component.text(message), Cause.of(EventContext.empty(), ""))
    }

    // TODO: 2021/7/11 可能存在争议的写法
    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(Sound.sound(Key.key(sound), Sound.Source.MASTER, volume, pitch), Vector3d.from(location.x, location.y, location.z))
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

    override fun sendRawMessage(message: String) {
        player.sendMessage(GsonComponentSerializer.gson().deserialize(message))
    }

    override fun sendParticle(particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?) {
        error("unsupported")
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun performCommand(command: String): Boolean {
        return Sponge.server().commandManager().process(player, command).isSuccess
    }

    // TODO: 2021/7/7 可能存在争议
    // 不确定的世界实例获取方式：ResourceKey.minecraft
    override fun teleport(loc: Location) {
        val serverWorld = Sponge.server().worldManager().world(ResourceKey.resolve(loc.world!!)).get()
        val serverLocation = ServerLocation.of(serverWorld, loc.x, loc.y, loc.z)
        player.setLocationAndRotation(serverLocation, Vector3d.from(loc.pitch.toDouble(), loc.yaw.toDouble(), 0.0))
    }
}
