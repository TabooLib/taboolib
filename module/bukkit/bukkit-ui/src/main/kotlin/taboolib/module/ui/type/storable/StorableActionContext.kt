package taboolib.module.ui.type.storable

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.type.impl.StorableChestImpl.RuleImpl
import org.bukkit.event.inventory.ClickType as BukkitClickType

/**
 * 操作上下文，封装所有操作需要的信息
 */
class StorableActionContext(
    /** 原始事件 */
    val event: ClickEvent,
    /** 容器 */
    val inventory: Inventory,
    /** 操作槽位 */
    val slot: Int,
    /** 光标物品 */
    val cursor: ItemStack?,
    /** 槽位物品 */
    val slotItem: ItemStack?,
    /** Bukkit 操作类型 */
    val action: InventoryAction,
    /** Bukkit 点击类型 */
    val clickType: BukkitClickType,
    /** 规则 */
    val rule: RuleImpl
) {
    /** 玩家 */
    val player: Player get() = event.clicker
    
    /** 快捷栏按键（NUMBER_KEY 时有效） */
    val hotbarKey: Int get() = event.hotbarKey
}
