package taboolib.platform.type

import com.flowpowered.math.vector.Vector3d
import com.google.common.base.Preconditions
import org.spongepowered.api.Sponge
import org.spongepowered.api.block.BlockState
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleOptions
import org.spongepowered.api.effect.particle.ParticleType
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.entity.living.player.gamemode.GameModes
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.EventContext
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.service.permission.SubjectData
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.serializer.TextSerializers
import org.spongepowered.api.text.title.Title
import org.spongepowered.api.util.Color
import org.spongepowered.api.util.Direction
import org.spongepowered.api.util.Tristate
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
 * taboolib.platform.type.SpongePlayer
 *
 * @author tr
 * @since 2021/6/21 15:49
 */
class Sponge7Player(val player: Player) : ProxyPlayer {

    override val origin: Any
        get() = player

    override val name: String
        get() = player.name

    override val address: InetSocketAddress?
        get() = player.connection.address

    override val uniqueId: UUID
        get() = player.uniqueId

    override val ping: Int
        get() = player.connection.latency

    override val locale: String
        get() = player.locale.displayName

    override val world: String
        get() = player.world.name

    override val location: Location
        get() {
            val loc = player.location
            return Location(world, loc.x, loc.y, loc.z, player.headRotation.y.toFloat(), player.headRotation.x.toFloat())
        }

    override var isOp: Boolean
        get() = player.hasPermission("*")
        set(value) {
            player.subjectData.setPermission(SubjectData.GLOBAL_CONTEXT, "*", if (value) Tristate.TRUE else Tristate.UNDEFINED)
        }

    override var compassTarget: Location
        get() {
            val vector3d = player.get(Keys.TARGETED_LOCATION).get()
            return Location(world, vector3d.x, vector3d.y, vector3d.z)
        }
        set(value) {
            player.offer(Keys.TARGETED_LOCATION, Vector3d(value.x, value.y, value.z))
        }

    override var bedSpawnLocation: Location?
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var displayName: String?
        get() = player.displayNameData.displayName().get().toPlain()
        set(value) {
            player.displayNameData.displayName().set(Text.of(value ?: ""))
        }

    override var playerListName: String?
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override var gameMode: ProxyGameMode
        get() = ProxyGameMode.fromString(player.gameMode().get().name)
        set(value) {
            player.gameMode().set(GameModes::class.java.getProperty(value.name, isStatic = true)!!)
        }

    override val isSneaking: Boolean
        get() = player.getOrElse(Keys.IS_SNEAKING, false)

    override val isSprinting: Boolean
        get() = player.getOrElse(Keys.IS_SPRINTING, false)

    override val isBlocking: Boolean
        get() {
            val item = player.get(Keys.ACTIVE_ITEM).get()
            return !item.isEmpty && item.type == ItemTypes.SHIELD
        }

    override var isGliding: Boolean
        get() = player.getOrElse(Keys.IS_ELYTRA_FLYING, false)
        set(value) {
            player.offer(Keys.IS_ELYTRA_FLYING, value)
        }

    override var isGlowing: Boolean
        get() = player.getOrElse(Keys.GLOWING, false)
        set(value) {
            player.offer(Keys.GLOWING, value)
        }

    override var isSwimming: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val isRiptiding: Boolean
        get() = error("unsupported")

    override val isSleeping: Boolean
        get() = player.getOrElse(Keys.IS_SLEEPING, false)

    override val sleepTicks: Int
        get() = error("unsupported")

    override var isSleepingIgnored: Boolean
        get() = player.isSleepingIgnored
        set(value) {
            player.isSleepingIgnored = value
        }

    override val isDead: Boolean
        get() = player.health().get() <= 0

    override val isConversing: Boolean
        get() = error("unsupported")

    override val isLeashed: Boolean
        get() = error("unsupported")

    override val isOnGround: Boolean
        get() = player.isOnGround

    override val isInsideVehicle: Boolean
        get() = player.vehicle.isPresent

    override var hasGravity: Boolean
        get() = player.gravity().get()
        set(value) {
            player.offer(Keys.HAS_GRAVITY, value)
        }

    override val attackCooldown: Int
        get() = error("unsupported")

    override var playerTime: Long
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override val firstPlayed: Long
        get() = player.firstPlayed().get().toEpochMilli()

    override val lastPlayed: Long
        get() = player.lastPlayed().get().toEpochMilli()

    override var absorptionAmount: Double
        get() = player.getOrElse(Keys.ABSORPTION, 0.0)
        set(value) {
            player.offer(Keys.ABSORPTION, value)
        }

    override var noDamageTicks: Int
        get() = player.getOrElse(Keys.INVULNERABILITY_TICKS, 0)
        set(value) {
            player.offer(Keys.INVULNERABILITY_TICKS, value)
        }

    override var remainingAir: Int
        get() = player.getOrElse(Keys.REMAINING_AIR, 0)
        set(value) {
            player.offer(Keys.REMAINING_AIR, value)
        }

    override val maximumAir: Int
        get() = player.getOrElse(Keys.MAX_AIR, 0)

    override var level: Int
        get() = player.getOrElse(Keys.EXPERIENCE_LEVEL, 0)
        set(value) {
            player.offer(Keys.EXPERIENCE_LEVEL, value)
        }

    override var exp: Float
        get() = player.getOrElse(Keys.EXPERIENCE_SINCE_LEVEL, 0) / player.getOrElse(Keys.EXPERIENCE_FROM_START_OF_LEVEL, 0).toFloat()
        set(value) {
            Preconditions.checkArgument(value in 0f..1f)
            player.offer(Keys.EXPERIENCE_SINCE_LEVEL, (player.getOrElse(Keys.EXPERIENCE_FROM_START_OF_LEVEL, 0) * value).toInt())
        }

    override var exhaustion: Float
        get() = player.getOrElse(Keys.EXHAUSTION, 0.0).toFloat()
        set(value) {
            player.offer(Keys.EXHAUSTION, value.toDouble())
        }

    override var saturation: Float
        get() = player.getOrElse(Keys.SATURATION, 0.0).toFloat()
        set(value) {
            player.offer(Keys.SATURATION, value.toDouble())
        }

    override var foodLevel: Int
        get() = player.getOrElse(Keys.FOOD_LEVEL, 0)
        set(value) {
            player.offer(Keys.FOOD_LEVEL, value)
        }

    override var health: Double
        get() = player.getOrElse(Keys.HEALTH, 0.0)
        set(value) {
            player.offer(Keys.HEALTH, value)
        }

    override var maxHealth: Double
        get() = player.getOrElse(Keys.MAX_HEALTH, 0.0)
        set(value) {
            player.offer(Keys.MAX_HEALTH, value)
        }

    override var allowFlight: Boolean
        get() = player.getOrElse(Keys.CAN_FLY, false)
        set(it) {
            player.offer(Keys.CAN_FLY, it)
        }

    override var isFlying: Boolean
        get() = player.getOrElse(Keys.IS_FLYING, false)
        set(it) {
            player.offer(Keys.IS_FLYING, it)
        }

    override var flySpeed: Float
        get() = player.getOrElse(Keys.FLYING_SPEED, 0.0).toFloat()
        set(it) {
            player.offer(Keys.FLYING_SPEED, it.toDouble())
        }

    override var walkSpeed: Float
        get() = player.getOrElse(Keys.WALKING_SPEED, 0.0).toFloat()
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
        get() = Direction.getClosest(player.transform.rotation).name

    override fun isOnline(): Boolean {
        return player.isOnline
    }

    override fun kick(message: String?) {
        player.kick(Text.of(message ?: ""))
    }

    override fun chat(message: String) {
        player.simulateChat(Text.of(message), Cause.of(EventContext.empty(), ""))
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(SoundType.of(sound), Vector3d.from(location.x, location.y, location.z), volume.toDouble(), pitch.toDouble())
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        playSound(location, sound, volume, pitch)
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        player.sendTitle(Title.builder().title(Text.of(title ?: "")).subtitle(Text.of(subtitle ?: "")).fadeIn(fadein).stay(stay).fadeOut(fadeout).build())
    }

    override fun sendActionBar(message: String) {
        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(message))
    }

    override fun sendMessage(message: String) {
        player.sendMessage(Text.of(message))
    }

    // 2021/7/6 Sponge Raw Message
    override fun sendRawMessage(message: String) {
        player.sendMessage(TextSerializers.JSON.deserialize(message))
    }

    override fun sendParticle(particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?) {
        if (particle.aliases[0] == "~") {
            error("Unsupported particle ${particle.name}")
        }
        var type: ParticleType? = null
        for (alias in particle.aliases) {
            try {
                type = if (alias == "@") {
                    ParticleTypes::class.java.getProperty<ParticleType>(particle.name, isStatic = true)
                } else {
                    ParticleTypes::class.java.getProperty<ParticleType>(alias, isStatic = true)
                }
            } catch (ignored: NoSuchFieldException) {
            }
        }
        if (type == null) {
            error("Unsupported particle ${particle.name}")
        }
        val builder = ParticleEffect.builder().type(type)
            .offset(Vector3d.from(offset.x, offset.y, offset.z))
            .velocity(Vector3d.from(speed))
            .quantity(count)
        when (data) {
            is ProxyParticle.DustData -> {
                builder.option(ParticleOptions.COLOR, Color.ofRgb(data.color.rgb))
            }
            is ProxyParticle.ItemData -> {
                try {
                    val itemStack = ItemStack.of(ItemTypes::class.java.getProperty(data.material)!!, data.data)
                    itemStack.offer(Keys.DISPLAY_NAME, Text.of(data.name))
                    itemStack.offer(Keys.ITEM_LORE, data.lore.map { Text.of(it) })
                    builder.option(ParticleOptions.ITEM_STACK_SNAPSHOT, itemStack.createSnapshot())
                } catch (ignored: NoSuchFieldException) {
                }
            }
            is ProxyParticle.BlockData -> {
                try {
                    builder.option(ParticleOptions.BLOCK_STATE, BlockState.builder().blockType(BlockTypes::class.java.getProperty(data.material)!!).build())
                } catch (ignored: NoSuchFieldException) {
                }
            }
        }
        player.spawnParticles(builder.build(), Vector3d.from(location.x, location.y, location.z))
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun performCommand(command: String): Boolean {
        return Sponge.getCommandManager().process(player, command).successCount.isPresent
    }

    // 2021/7/7 可能存在争议
    // 应当保留 Location 接口中的 yaw，pitch 属性
    override fun teleport(loc: Location) {
        val world = Sponge.getServer().getWorld(loc.world ?: return).orElseThrow { Exception() }
        val location = org.spongepowered.api.world.Location(world, Vector3d.from(loc.x, loc.y, loc.z))
        player.setLocationAndRotation(location, Vector3d.from(loc.pitch.toDouble(), loc.yaw.toDouble(), 0.0))
    }
}
