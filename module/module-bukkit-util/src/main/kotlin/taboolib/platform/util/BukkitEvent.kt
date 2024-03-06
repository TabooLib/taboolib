package taboolib.platform.util

import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

fun PlayerInteractEvent.isRightClick() = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK

fun PlayerInteractEvent.isRightClickAir() = action == Action.RIGHT_CLICK_AIR

fun PlayerInteractEvent.isRightClickBlock() = action == Action.RIGHT_CLICK_BLOCK

fun PlayerInteractEvent.isLeftClick() = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK

fun PlayerInteractEvent.isLeftClickAir() = action == Action.LEFT_CLICK_AIR

fun PlayerInteractEvent.isLeftClickBlock() = action == Action.LEFT_CLICK_BLOCK

fun PlayerInteractEvent.isPhysical() = action == Action.PHYSICAL

fun PlayerInteractEvent.isMainhand() = hand == EquipmentSlot.HAND

fun PlayerInteractEvent.isOffhand() = hand == EquipmentSlot.OFF_HAND

fun PlayerInteractEntityEvent.isMainhand() = hand == EquipmentSlot.HAND

fun PlayerInteractEntityEvent.isOffhand() = hand == EquipmentSlot.OFF_HAND

fun PlayerMoveEvent.isMovement() = to != null && (from.x != to!!.x || from.z != to!!.z)

fun PlayerMoveEvent.isBlockMovement() = to != null && (from.blockX != to!!.blockX || from.blockZ != to!!.blockZ)

fun PlayerMoveEvent.isVerticalMovement() = to != null && from.y != to!!.y

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

enum class MoveDirection {

    FORWARD, BACKWARD, LEFT, RIGHT
}