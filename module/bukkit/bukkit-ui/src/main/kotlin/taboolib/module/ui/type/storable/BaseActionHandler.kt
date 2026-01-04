package taboolib.module.ui.type.storable

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.isAir

/**
 * 操作处理器基类
 * 提供公共的检查和操作方法
 */
abstract class BaseActionHandler {
    
    /**
     * 检查物品是否可以放入指定槽位
     */
    protected fun canPlace(ctx: StorableActionContext, item: ItemStack?): Boolean {
        if (item == null || item.isAir) return true
        return ctx.rule.canPlace(ctx.inventory, item, ctx.slot)
    }
    
    /**
     * 检查物品是否可以从指定槽位取出（装饰物保护）
     */
    protected fun canPickup(ctx: StorableActionContext, item: ItemStack?): Boolean {
        if (item == null || item.isAir) return true
        return ctx.rule.canPlace(ctx.inventory, item, ctx.slot)
    }
    
    /**
     * 写入槽位
     */
    protected fun writeSlot(ctx: StorableActionContext, item: ItemStack?) {
        ctx.rule.setItem(ctx.inventory, item ?: ItemStack(Material.AIR), ctx.slot, ctx.clickType)
    }
    
    /**
     * 清空槽位
     */
    protected fun clearSlot(ctx: StorableActionContext) {
        writeSlot(ctx, ItemStack(Material.AIR))
    }
    
    /**
     * 设置光标物品
     */
    protected fun setCursor(ctx: StorableActionContext, item: ItemStack?) {
        ctx.player.setItemOnCursor(item)
    }
    
    /**
     * 更新光标数量，如果数量为0则清空
     */
    protected fun updateCursor(ctx: StorableActionContext, item: ItemStack, newAmount: Int) {
        if (newAmount <= 0) {
            setCursor(ctx, null)
        } else {
            setCursor(ctx, item.clone().apply { amount = newAmount })
        }
    }
    
    /**
     * 获取物品的最大堆叠数
     */
    protected fun getMaxStack(ctx: StorableActionContext, item: ItemStack): Int {
        return ctx.rule.getItemStacker().getMaxStackSize(item)
    }
    
    /**
     * 计算合并结果
     * @return Pair<槽位物品数量, 光标剩余数量>
     */
    protected fun calculateMerge(slotAmount: Int, cursorAmount: Int, maxStack: Int): Pair<Int, Int> {
        val total = slotAmount + cursorAmount
        return if (total <= maxStack) {
            Pair(total, 0)
        } else {
            Pair(maxStack, total - maxStack)
        }
    }
}
