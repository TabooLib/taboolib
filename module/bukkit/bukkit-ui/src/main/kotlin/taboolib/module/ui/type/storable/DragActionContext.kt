package taboolib.module.ui.type.storable

import org.bukkit.entity.Player
import org.bukkit.event.inventory.DragType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.submit
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.type.impl.StorableChestImpl.RuleImpl

/**
 * 拖拽操作上下文
 */
class DragActionContext(
    val event: ClickEvent,
    val inventory: Inventory,
    val slot: Int,
    val oldCursor: ItemStack,
    val slotItem: ItemStack?,
    val dragType: DragType,
    val rule: RuleImpl
) {
    val player: Player get() = event.clicker
    
    /** 获取 newItems 中对应槽位的物品（可修改） */
    val newItem: ItemStack? get() = event.dragEvent().newItems[slot]
    
    /** 设置拖拽后的光标 */
    fun setCursor(item: ItemStack?) {
        event.dragEvent().cursor = item
    }
    
    /** 修改 newItem 的属性使其与指定物品一致 */
    fun syncNewItem(item: ItemStack) {
        val ni = newItem
        if (ni != null) {
            ni.type = item.type
            ni.amount = item.amount
            ni.itemMeta = item.itemMeta
        } else {
            inventory.setItem(slot, item)
        }
    }
}
