package taboolib.module.ui.type.storable

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.isAir

/**
 * Shift 点击操作处理器
 */
class ShiftClickHandler : BaseActionHandler() {
    
    /**
     * 从玩家背包 Shift 点击到 UI
     */
    fun shiftClickToUI(ctx: StorableActionContext, autoStack: Boolean): StorableActionResult {
        val currentItem = ctx.event.currentItem ?: return StorableActionResult.DENIED
        if (currentItem.isAir) return StorableActionResult.DENIED
        return if (autoStack) {
            shiftClickWithAutoStack(ctx, currentItem)
        } else {
            shiftClickWithoutAutoStack(ctx, currentItem)
        }
    }
    
    private fun shiftClickWithAutoStack(ctx: StorableActionContext, currentItem: ItemStack): StorableActionResult {
        val inventory = ctx.inventory
        val remainingItem = currentItem.clone()
        val slots = ctx.rule.getMergeSlots(inventory, remainingItem) ?: (0 until inventory.size).toList()
        // 第一步：尝试合并到所有相同物品的槽位
        for (slot in slots) {
            if (remainingItem.amount <= 0) break
            val slotItem = ctx.rule.getItem(inventory, slot)
            if (slotItem != null && !slotItem.isAir && slotItem.isSimilar(remainingItem)) {
                val maxStack = ctx.rule.getItemStacker().getMaxStackSize(slotItem)
                if (slotItem.amount < maxStack) {
                    val toAdd = minOf(maxStack - slotItem.amount, remainingItem.amount)
                    ctx.rule.setItem(inventory, slotItem.clone().apply { amount = slotItem.amount + toAdd }, slot, ctx.clickType)
                    remainingItem.amount -= toAdd
                }
            }
        }
        // 第二步：如果还有剩余，找空槽位放入
        if (remainingItem.amount > 0) {
            val firstSlot = ctx.rule.getFirstSlot(inventory, remainingItem)
            if (firstSlot >= 0 && ctx.rule.getItem(inventory, firstSlot).isAir) {
                ctx.rule.setItem(inventory, remainingItem.clone(), firstSlot, ctx.clickType)
                remainingItem.amount = 0
            }
        }
        // 第三步：更新玩家背包中的物品
        if (remainingItem.amount <= 0) {
            ctx.event.currentItem?.type = Material.AIR
            ctx.event.currentItem = null
        } else {
            currentItem.amount = remainingItem.amount
        }
        return StorableActionResult.HANDLED
    }
    
    private fun shiftClickWithoutAutoStack(ctx: StorableActionContext, currentItem: ItemStack): StorableActionResult {
        val inventory = ctx.inventory
        val firstSlot = ctx.rule.getFirstSlot(inventory, currentItem)
        if (firstSlot >= 0) {
            if (ctx.rule.canShiftSwap(inventory, currentItem, firstSlot)) {
                ctx.event.currentItem = ctx.rule.getItem(inventory, firstSlot)
                ctx.rule.setItem(inventory, currentItem, firstSlot, ctx.clickType)
            } else if (ctx.rule.getItem(inventory, firstSlot).isAir) {
                ctx.rule.setItem(inventory, currentItem, firstSlot, ctx.clickType)
                ctx.event.currentItem?.type = Material.AIR
                ctx.event.currentItem = null
            }
        }
        return StorableActionResult.HANDLED
    }
    
    /**
     * 从 UI Shift 点击到玩家背包
     */
    fun shiftClickFromUI(ctx: StorableActionContext): StorableActionResult {
        val slotItem = ctx.slotItem ?: return StorableActionResult.DENIED
        if (slotItem.isAir) return StorableActionResult.DENIED
        if (!canPickup(ctx, slotItem)) return StorableActionResult.DENIED
        clearSlot(ctx)
        val remaining = ctx.player.inventory.addItem(slotItem.clone())
        // 如果背包满了，把剩余物品放回槽位
        if (remaining.isNotEmpty()) {
            writeSlot(ctx, remaining.values.first())
        }
        return StorableActionResult.HANDLED
    }
}
