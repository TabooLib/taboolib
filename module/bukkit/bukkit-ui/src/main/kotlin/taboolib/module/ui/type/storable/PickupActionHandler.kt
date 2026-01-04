package taboolib.module.ui.type.storable

import taboolib.platform.util.isAir

/**
 * 取出操作处理器
 * 处理 PICKUP_ONE, PICKUP_HALF, PICKUP_ALL
 */
class PickupActionHandler : BaseActionHandler() {
    
    /**
     * 取出一个物品
     */
    fun pickupOne(ctx: StorableActionContext): StorableActionResult {
        val slotItem = ctx.slotItem ?: return StorableActionResult.DENIED
        if (slotItem.isAir) return StorableActionResult.DENIED
        if (!canPickup(ctx, slotItem)) return StorableActionResult.DENIED
        if (slotItem.amount > 1) {
            writeSlot(ctx, slotItem.clone().apply { amount = slotItem.amount - 1 })
            setCursor(ctx, slotItem.clone().apply { amount = 1 })
        } else {
            clearSlot(ctx)
            setCursor(ctx, slotItem.clone())
        }
        return StorableActionResult.HANDLED
    }
    
    /**
     * 取出一半物品
     */
    fun pickupHalf(ctx: StorableActionContext): StorableActionResult {
        val slotItem = ctx.slotItem ?: return StorableActionResult.DENIED
        if (slotItem.isAir) return StorableActionResult.DENIED
        if (!canPickup(ctx, slotItem)) return StorableActionResult.DENIED
        // 向上取整给光标
        val halfAmount = (slotItem.amount + 1) / 2
        val remainingAmount = slotItem.amount - halfAmount
        if (remainingAmount > 0) {
            writeSlot(ctx, slotItem.clone().apply { amount = remainingAmount })
        } else {
            clearSlot(ctx)
        }
        setCursor(ctx, slotItem.clone().apply { amount = halfAmount })
        return StorableActionResult.HANDLED
    }
    
    /**
     * 取出全部物品
     */
    fun pickupAll(ctx: StorableActionContext): StorableActionResult {
        val slotItem = ctx.slotItem ?: return StorableActionResult.DENIED
        if (slotItem.isAir) return StorableActionResult.DENIED
        if (!canPickup(ctx, slotItem)) return StorableActionResult.DENIED
        clearSlot(ctx)
        setCursor(ctx, slotItem.clone())
        return StorableActionResult.HANDLED
    }
}
