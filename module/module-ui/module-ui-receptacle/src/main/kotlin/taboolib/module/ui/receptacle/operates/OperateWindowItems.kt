package taboolib.module.ui.receptacle.operates

import org.bukkit.inventory.ItemStack

/**
 * @author Arasple
 * @date 2020/12/4 21:55
 *
 * Sent by the server when items in multiple slots (in a window) are added/removed.
 * This includes the main inventory, equipped armour and crafting slots.
 *
 * The ID of window which items are being sent for. 0 for player inventory.
 */
class OperateWindowItems(val items: Array<ItemStack?>, override val packet: Boolean = true) : OperateInventory() {

    val windowId: Int = if (packet) 119 else 120
}