package taboolib.common.platform

import taboolib.common.util.Location
import taboolib.common.util.Vector
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.common.platform.ProxyPlayer
 *
 * @author sky
 * @since 2021/6/17 12:03 上午
 */
interface ProxyPlayer : ProxyCommandSender {

    val address: InetSocketAddress?

    val uniqueId: UUID

    val ping: Int

    val locale: String

    val world: String

    val location: Location

    var compassTarget: Location

    var bedSpawnLocation: Location?

    var displayName: String?

    var playerListName: String?

    var gameMode: ProxyGameMode

    val isSneaking: Boolean

    val isSprinting: Boolean

    val isBlocking: Boolean

    var isGliding: Boolean

    var isGlowing: Boolean

    var isSwimming: Boolean

    val isRiptiding: Boolean

    val isSleeping: Boolean

    val sleepTicks: Int

    var isSleepingIgnored: Boolean

    val isDead: Boolean

    val isConversing: Boolean

    val isLeashed: Boolean

    val isOnGround: Boolean

    val isInsideVehicle: Boolean

    var hasGravity: Boolean

    val attackCooldown: Int

    var playerTime: Long

    val firstPlayed: Long

    val lastPlayed: Long

    var absorptionAmount: Double

    var noDamageTicks: Int

    var remainingAir: Int

    val maximumAir: Int

    var level: Int

    var exp: Float

    var exhaustion: Float

    var saturation: Float

    var foodLevel: Int

    var health: Double

    var maxHealth: Double

    var allowFlight: Boolean

    var isFlying: Boolean

    var flySpeed: Float

    var walkSpeed: Float

    val pose: String

    val facing: String

    fun kick(message: String?)

    fun chat(message: String)

    fun playSound(location: Location, sound: String, volume: Float, pitch: Float)

    fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float)

    fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int)

    fun sendActionBar(message: String)

    fun sendRawMessage(message: String)

    fun sendParticle(particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?)

    fun teleport(loc: Location)
}