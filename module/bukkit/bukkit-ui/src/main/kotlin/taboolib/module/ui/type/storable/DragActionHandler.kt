package taboolib.module.ui.type.storable

import org.bukkit.event.inventory.DragType
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.ClickEvent
import taboolib.platform.util.isAir
import org.bukkit.event.inventory.ClickType as BukkitClickType

/**
 * 拖拽操作处理器
 * 注意：drag 事件不能取消，否则 cursor 无法设置
 */
class DragActionHandler : BaseActionHandler() {

    /**
     * 处理单格拖拽（映射为点击行为）
     */
    fun handleSingleSlotDrag(ctx: DragActionContext): StorableActionResult {
        // 检查是否允许放入
        if (!ctx.rule.canPlace(ctx.inventory, ctx.oldCursor, ctx.slot)) {
            return StorableActionResult.DENIED
        }
        return if (ctx.dragType == DragType.SINGLE) {
            handleRightDrag(ctx)
        } else {
            handleLeftDrag(ctx)
        }
    }

    /**
     * 右键拖拽：只放 1 个
     */
    private fun handleRightDrag(ctx: DragActionContext): StorableActionResult {
        val slotItem = ctx.slotItem
        val oldCursor = ctx.oldCursor
        when {
            // 空槽位：放 1 个
            slotItem.isAir -> {
                val itemToPlace = oldCursor.clone().apply { amount = 1 }
                ctx.rule.setItem(ctx.inventory, itemToPlace, ctx.slot, BukkitClickType.RIGHT)
                setCursorDrag(ctx, oldCursor, 1)
            }
            // 相同物品：合并 1 个
            slotItem != null && slotItem.isSimilar(oldCursor) -> {
                val maxStack = ctx.rule.getItemStacker().getMaxStackSize(slotItem)
                if (slotItem.amount >= maxStack) {
                    return StorableActionResult.DENIED
                }
                val merged = slotItem.clone().apply { amount = slotItem.amount + 1 }
                ctx.rule.setItem(ctx.inventory, merged, ctx.slot, BukkitClickType.RIGHT)
                ctx.newItem?.amount = merged.amount
                setCursorDrag(ctx, oldCursor, 1)
            }
            // 不同物品：交换
            else -> {
                if (slotItem != null && !ctx.rule.canPlace(ctx.inventory, slotItem, ctx.slot)) {
                    return StorableActionResult.DENIED
                }
                ctx.rule.setItem(ctx.inventory, oldCursor, ctx.slot, BukkitClickType.RIGHT)
                ctx.syncNewItem(oldCursor)
                ctx.setCursor(slotItem)
            }
        }
        return StorableActionResult.HANDLED
    }

    /**
     * 左键拖拽：放置全部或交换
     */
    private fun handleLeftDrag(ctx: DragActionContext): StorableActionResult {
        val slotItem = ctx.slotItem
        val oldCursor = ctx.oldCursor
        when {
            // 空槽位：放置全部
            slotItem.isAir -> {
                ctx.rule.setItem(ctx.inventory, oldCursor.clone(), ctx.slot, BukkitClickType.LEFT)
                ctx.syncNewItem(oldCursor)
                ctx.setCursor(null)
            }
            // 相同物品：尝试合并
            slotItem != null && slotItem.isSimilar(oldCursor) -> {
                val maxStack = ctx.rule.getItemStacker().getMaxStackSize(slotItem)
                val total = slotItem.amount + oldCursor.amount
                if (total <= maxStack) {
                    val merged = slotItem.clone().apply { amount = total }
                    ctx.rule.setItem(ctx.inventory, merged, ctx.slot, BukkitClickType.LEFT)
                    ctx.syncNewItem(merged)
                    ctx.setCursor(null)
                } else {
                    val merged = slotItem.clone().apply { amount = maxStack }
                    ctx.rule.setItem(ctx.inventory, merged, ctx.slot, BukkitClickType.LEFT)
                    ctx.syncNewItem(merged)
                    ctx.setCursor(oldCursor.clone().apply { amount = total - maxStack })
                }
            }
            // 不同物品：交换
            else -> {
                if (slotItem != null && !ctx.rule.canPlace(ctx.inventory, slotItem, ctx.slot)) {
                    return StorableActionResult.DENIED
                }
                ctx.rule.setItem(ctx.inventory, oldCursor.clone(), ctx.slot, BukkitClickType.LEFT)
                ctx.syncNewItem(oldCursor)
                ctx.setCursor(slotItem)
            }
        }
        return StorableActionResult.HANDLED
    }

    /**
     * 处理多格拖拽
     */
    fun handleMultiSlotDrag(event: ClickEvent, inventorySize: Int): StorableActionResult {
        val hasSlotInUI = event.dragEvent().rawSlots.any { it < inventorySize }
        return if (hasSlotInUI) StorableActionResult.DENIED else StorableActionResult.PASS
    }

    private fun setCursorDrag(ctx: DragActionContext, oldCursor: ItemStack, amountUsed: Int) {
        ctx.setCursor(if (oldCursor.amount > amountUsed) oldCursor.clone().apply { amount = oldCursor.amount - amountUsed } else null)
    }
}
