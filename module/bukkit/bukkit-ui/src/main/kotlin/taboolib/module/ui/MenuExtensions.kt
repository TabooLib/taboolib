package taboolib.module.ui

import org.bukkit.event.inventory.InventoryCloseEvent
import taboolib.platform.util.giveItem

/**
 * 在页面关闭时返还物品
 */
fun InventoryCloseEvent.returnItems(slots: List<Int>) = slots.forEach { player.giveItem(inventory.getItem(it)) }