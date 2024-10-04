package taboolib.platform.util

import org.bukkit.entity.EvokerFangs
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

/**
 * 检查 [PlayerInteractEvent] 是否为右键点击（空气或方块）。
 *
 * @return 如果是右键点击则返回 true，否则返回 false。
 */
fun PlayerInteractEvent.isRightClick() = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK

/**
 * 检查 [PlayerInteractEvent] 是否为右键点击空气。
 *
 * @return 如果是右键点击空气则返回 true，否则返回 false。
 */
fun PlayerInteractEvent.isRightClickAir() = action == Action.RIGHT_CLICK_AIR

/**
 * 检查 [PlayerInteractEvent] 是否为右键点击方块。
 *
 * @return 如果是右键点击方块则返回 true，否则返回 false。
 */
fun PlayerInteractEvent.isRightClickBlock() = action == Action.RIGHT_CLICK_BLOCK

/**
 * 检查 [PlayerInteractEvent] 是否为左键点击（空气或方块）。
 *
 * @return 如果是左键点击则返回 true，否则返回 false。
 */
fun PlayerInteractEvent.isLeftClick() = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK

/**
 * 检查 [PlayerInteractEvent] 是否为左键点击空气。
 *
 * @return 如果是左键点击空气则返回 true，否则返回 false。
 */
fun PlayerInteractEvent.isLeftClickAir() = action == Action.LEFT_CLICK_AIR

/**
 * 检查 [PlayerInteractEvent] 是否为左键点击方块。
 *
 * @return 如果是左键点击方块则返回 true，否则返回 false。
 */
fun PlayerInteractEvent.isLeftClickBlock() = action == Action.LEFT_CLICK_BLOCK

/**
 * 检查 [PlayerInteractEvent] 是否为物理交互（如踩压力板）。
 *
 * @return 如果是物理交互则返回 true，否则返回 false。
 */
fun PlayerInteractEvent.isPhysical() = action == Action.PHYSICAL

/**
 * 检查 [PlayerInteractEvent] 是否使用主手进行交互。
 *
 * @return 如果使用主手则返回 true，否则返回 false。
 */
fun PlayerInteractEvent.isMainhand() = hand == EquipmentSlot.HAND

/**
 * 检查 [PlayerInteractEvent] 是否使用副手进行交互。
 *
 * @return 如果使用副手则返回 true，否则返回 false。
 */
fun PlayerInteractEvent.isOffhand() = hand == EquipmentSlot.OFF_HAND

/**
 * 检查 [PlayerInteractEntityEvent] 是否使用主手进行交互。
 *
 * @return 如果使用主手则返回 true，否则返回 false。
 */
fun PlayerInteractEntityEvent.isMainhand() = hand == EquipmentSlot.HAND

/**
 * 检查 [PlayerInteractEntityEvent] 是否使用副手进行交互。
 *
 * @return 如果使用副手则返回 true，否则返回 false。
 */
fun PlayerInteractEntityEvent.isOffhand() = hand == EquipmentSlot.OFF_HAND

/**
 * 检查 [PlayerMoveEvent] 是否发生了水平移动。
 *
 * @return 如果发生了水平移动则返回 true，否则返回 false。
 */
fun PlayerMoveEvent.isMovement() = to != null && (from.x != to!!.x || from.z != to!!.z)

/**
 * 检查 [PlayerMoveEvent] 是否发生了整数坐标的水平移动。
 *
 * @return 如果发生了整数坐标的水平移动则返回 true，否则返回 false。
 */
fun PlayerMoveEvent.isBlockMovement() = to != null && (from.blockX != to!!.blockX || from.blockZ != to!!.blockZ)

/**
 * 检查 [PlayerMoveEvent] 是否发生了垂直移动。
 *
 * @return 如果发生了垂直移动则返回 true，否则返回 false。
 */
fun PlayerMoveEvent.isVerticalMovement() = to != null && from.y != to!!.y

/**
 * 获取玩家移动的方向列表。
 *
 * 此函数分析 [PlayerMoveEvent] 中的移动数据，并返回一个包含玩家移动方向的列表。
 * 移动方向可能包括前进、后退、左移和右移。
 *
 * @return 包含 [MoveDirection] 枚举值的列表，表示玩家的移动方向。
 *         如果玩家没有移动，则返回空列表。
 */
fun PlayerMoveEvent.moveDirection(): List<MoveDirection> {
    val moveDirection = ArrayList<MoveDirection>()
    if (isMovement()) {
        val from = from.toVector()
        val to = to?.toVector() ?: return emptyList()
        // 获取玩家移动的向量
        val moveVector = to.subtract(from).normalize()
        // 将玩家的朝向转换为向量
        val yaw = player.location.yaw
        val directionVector = Vector(-sin(Math.toRadians(yaw.toDouble())), 0.0, cos(Math.toRadians(yaw.toDouble()))).normalize()
        // 计算右侧向量（朝向向量的顺时针 90 度旋转）
        val rightVector = Vector(-directionVector.z, 0.0, directionVector.x)
        // 计算移动向量在朝向向量和右侧向量上的投影长度
        val forwardProjection = moveVector.dot(directionVector)
        val rightProjection = moveVector.dot(rightVector)
        // 判断移动方向
        val movingForward = forwardProjection > 0.1 // 向前移动的阈值
        val movingBackward = forwardProjection < -0.1 // 向后移动的阈值
        val movingRight = rightProjection > 0.1 // 向右移动的阈值
        val movingLeft = rightProjection < -0.1 // 向左移动的阈值
        if (movingForward) moveDirection += MoveDirection.FORWARD
        if (movingBackward) moveDirection += MoveDirection.BACKWARD
        if (movingRight) moveDirection += MoveDirection.RIGHT
        if (movingLeft) moveDirection += MoveDirection.LEFT
    }
    return moveDirection
}

/**
 * 获取 [EntityDamageByEntityEvent] 事件中的攻击者。
 *
 * 此属性会尝试识别不同类型的攻击者，包括：
 * 1. 直接的生物实体攻击者
 * 2. 弹射物的发射者（如果是生物实体）
 * 3. 特定版本中的唤魔者尖牙（EvokerFangs）的所有者
 *
 * @return 攻击者实体，如果无法确定则返回 null
 */
val EntityDamageByEntityEvent.attacker: LivingEntity?
    get() {
        val attacker = damager
        return when {
            attacker is LivingEntity -> attacker
            // 弹射物
            attacker is Projectile && attacker.shooter is LivingEntity -> attacker.shooter as LivingEntity?
            // 版本兼容策略
            attacker.javaClass.simpleName == "EvokerFangs" && attacker is EvokerFangs -> attacker.owner
            // 其他
            else -> null
        }
    }

/**
 * 获取 [EntityDeathEvent] 事件中实体的杀手。
 * 此属性首先检查实体的直接杀手，如果没有，则尝试从最后一次伤害事件中获取攻击者。
 *
 * @return 杀死实体的生物实体，如果无法确定则返回 null
 */
val EntityDeathEvent.killer: LivingEntity?
    get() = entity.killer ?: (entity.lastDamageCause as? EntityDamageByEntityEvent)?.attacker

/**
 * 获取 [PlayerDeathEvent] 事件中玩家的杀手。
 * 此属性首先检查玩家的直接杀手，如果没有，则尝试从最后一次伤害事件中获取攻击者。
 *
 * @return 杀死玩家的生物实体，如果无法确定则返回 null
 */
val PlayerDeathEvent.killer: LivingEntity?
    get() = entity.killer ?: (entity.lastDamageCause as? EntityDamageByEntityEvent)?.attacker