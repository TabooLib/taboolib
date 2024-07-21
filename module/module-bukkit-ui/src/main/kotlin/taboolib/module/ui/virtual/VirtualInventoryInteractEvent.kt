package taboolib.module.ui.virtual

import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.inventory.InventoryView

/**
 * TabooLib
 * taboolib.module.ui.virtual.VirtualInventoryInteractEvent
 *
 * @author 坏黑
 * @since 2023/1/16 04:15
 */
class VirtualInventoryInteractEvent(val clickEvent: RemoteInventory.ClickEvent, inventoryView: InventoryView) : InventoryInteractEvent(inventoryView)