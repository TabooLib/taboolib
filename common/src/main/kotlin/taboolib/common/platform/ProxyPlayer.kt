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

    /**
     * 地址
     */
    val address: InetSocketAddress?

    /**
     * 唯一 ID
     */
    val uniqueId: UUID

    /**
     * 延迟
     */
    val ping: Int

    /**
     * 客户端语言
     */
    val locale: String

    /**
     * 所在世界名称
     */
    val world: String

    /**
     * 所在位置
     */
    val location: Location

    /**
     * 指南针指向未知
     */
    var compassTarget: Location

    /**
     * 床位置
     */
    var bedSpawnLocation: Location?

    /**
     * 展示名称
     */
    var displayName: String?

    /**
     * 列表名称
     */
    var playerListName: String?

    /**
     * 游戏模式
     */
    var gameMode: ProxyGameMode

    /**
     * 是否潜行
     */
    val isSneaking: Boolean

    /**
     * 是否疾跑
     */
    val isSprinting: Boolean

    /**
     * 是否格挡
     */
    val isBlocking: Boolean

    /**
     * 是否滑翔
     */
    var isGliding: Boolean

    /**
     * 是否发光
     */
    var isGlowing: Boolean

    /**
     * 是否游泳
     */
    var isSwimming: Boolean

    /**
     * 是否激流
     */
    val isRiptiding: Boolean

    /**
     * 是否睡觉
     */
    val isSleeping: Boolean

    /**
     * 睡眠时间
     */
    val sleepTicks: Int

    /**
     * 是否忽略睡眠限制
     */
    var isSleepingIgnored: Boolean

    /**
     * 是否死亡
     */
    val isDead: Boolean

    val isConversing: Boolean

    /**
     * 是否牵引
     */
    val isLeashed: Boolean

    /**
     * 是否在地面
     */
    val isOnGround: Boolean

    /**
     * 是否在载具
     */
    val isInsideVehicle: Boolean

    /**
     * 是否有重力
     */
    var hasGravity: Boolean

    /**
     * 攻击冷却
     */
    val attackCooldown: Int

    /**
     * 游戏时间
     */
    var playerTime: Long

    /**
     * 首次加入时间
     */
    val firstPlayed: Long

    /**
     * 最后一次加入时间
     */
    val lastPlayed: Long

    /**
     * 额外生命
     */
    var absorptionAmount: Double

    /**
     * 无敌时间
     */
    var noDamageTicks: Int

    /**
     * 氧气
     */
    var remainingAir: Int

    /**
     * 最大氧气
     */
    val maximumAir: Int

    /**
     * 等级
     */
    var level: Int

    /**
     * 经验
     */
    var exp: Float

    var exhaustion: Float

    /**
     * 饱食
     */
    var saturation: Float

    /**
     * 饥饿
     */
    var foodLevel: Int

    /**
     * 生命
     */
    var health: Double

    /**
     * 最大生命
     */
    var maxHealth: Double

    /**
     * 是否允许飞行
     */
    var allowFlight: Boolean

    /**
     * 是否在飞行
     */
    var isFlying: Boolean

    /**
     * 飞行速度
     */
    var flySpeed: Float

    /**
     * 行走速度
     */
    var walkSpeed: Float

    /**
     * 状态
     */
    val pose: String

    /**
     * 朝向
     */
    val facing: String

    /**
     * 踢出玩家
     *
     * @param message 原因
     */
    fun kick(message: String?)

    /**
     * 发送聊天信息
     *
     * @param message 信息
     */
    fun chat(message: String)

    /**
     * 播放音效
     *
     * @param location 位置
     * @param sound 音效
     * @param volume 音量
     * @param pitch 音调
     */
    fun playSound(location: Location, sound: String, volume: Float, pitch: Float)

    /**
     * 播放资源文件音效
     *
     * @param location 位置
     * @param sound 音效
     * @param volume 音量
     * @param pitch 音调
     */
    fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float)

    /**
     * 发送标题
     *
     * @param title 标题
     * @param subtitle 副标题
     * @param fadein 渐入时间
     * @param stay 停留时间
     * @param fadeout 渐出时间
     */
    fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int)

    /**
     * 发送动作栏信息
     *
     * @param message 信息
     */
    fun sendActionBar(message: String)

    /**
     * 发送原始信息（RawMessage）
     *
     * @param message 信息
     */
    fun sendRawMessage(message: String)

    /**
     * 发送粒子效果
     *
     * @param particle 粒子效果
     * @param location 位置
     * @param offset 偏移
     * @param count 数量
     * @param speed 速度
     * @param data 数据
     */
    fun sendParticle(particle: ProxyParticle, location: Location, offset: Vector, count: Int, speed: Double, data: ProxyParticle.Data?)

    /**
     * 传送
     *
     * @param location 位置
     */
    fun teleport(location: Location)

    /**
     * 给予经验
     *
     * @param exp 经验
     */
    fun giveExp(exp: Int)
}