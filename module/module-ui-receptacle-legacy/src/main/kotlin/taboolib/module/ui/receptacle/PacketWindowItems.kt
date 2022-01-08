package taboolib.module.ui.receptacle

import org.bukkit.inventory.ItemStack

/**
 * @author Arasple
 * @date 2020/12/4 21:55
 *
 * Sent by the server when items in multiple slots (in a window) are added/removed.
 * This includes the main inventory, equipped armour and crafting slots.
 *
 * The ID of window which items are being sent for. 0 for player inventory.
 *
 * @param items Array of Slot
 */
class PacketWindowItems(val items: Array<ItemStack?>) : PacketInventory {

    val windowId = 119
}