package taboolib.module.ui.type.storable

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.isNotAir

/**
 * 交换操作处理器
 * 处理 SWAP_WITH_CURSOR, HOTBAR_SWAP
 */
class SwapActionHandler : BaseActionHandler() {
    
    /**
     * 光标与槽位交换
     */
    fun swapWithCursor(ctx: StorableActionContext): StorableActionResult {
        val cursor = ctx.cursor
        val slotItem = ctx.slotItem
        if (!canPlace(ctx, cursor)) return StorableActionResult.DENIED
        if (!canPickup(ctx, slotItem)) return StorableActionResult.DENIED
        writeSlot(ctx, cursor?.clone())
        setCursor(ctx, slotItem?.clone())
        return StorableActionResult.HANDLED
    }
    
    /**
     * 数字键交换（快捷栏与槽位）
     */
    fun hotbarSwap(ctx: StorableActionContext): StorableActionResult {
        val hotbarSlot = ctx.hotbarKey
        if (hotbarSlot in 0..8) {
            val hotbarItem = ctx.player.inventory.getItem(hotbarSlot)
            val slotItem = ctx.slotItem
            // 检查快捷栏物品是否允许放入
            if (hotbarItem.isNotAir() && !ctx.rule.canPlace(ctx.inventory, hotbarItem!!, ctx.slot)) {
                return StorableActionResult.DENIED
            }
            if (canPickup(ctx, slotItem)) {
                writeSlot(ctx, hotbarItem?.clone() ?: ItemStack(Material.AIR))
                ctx.player.inventory.setItem(hotbarSlot, slotItem?.clone())
                return StorableActionResult.HANDLED
            }
            return StorableActionResult.DENIED
        }
        return StorableActionResult.DENIED
    }
}
