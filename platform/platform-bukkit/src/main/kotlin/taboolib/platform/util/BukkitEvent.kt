@file:Isolated
package taboolib.platform.util

import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import taboolib.common.Isolated

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