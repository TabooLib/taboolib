package taboolib.module.ui.receptacle

import org.bukkit.inventory.ItemStack

/**
 * @author Arasple
 * @date 2020/12/4 22:42
 *
 * Sent by the server when an item in a slot (in a window) is added/removed.
 *
 * The window which is being updated. 0 for player inventory.
 * Note that all known window types include the player inventory.
 * This packet will only be sent for the currently opened window while the player is performing actions,
 * even if it affects the player inventory.
 * After the window is closed, a number of these packets are sent to update the player's inventory window (0).
 *
 * @param slot The slot that should be updated
 * @param itemStack The to update item stack
 *
 */
class PacketWindowSetSlot(val slot: Int, val itemStack: ItemStack? = null, val windowId: Int = 119, val stateId: Int = 1) : PacketInventory {

}