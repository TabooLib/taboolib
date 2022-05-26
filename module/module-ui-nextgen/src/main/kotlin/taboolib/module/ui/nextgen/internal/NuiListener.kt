package taboolib.module.ui.nextgen.internal

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import taboolib.common.platform.event.SubscribeEvent

object NuiListener {
    object GlobalHolder : InventoryHolder {
        override fun getInventory(): Inventory {
            TODO("Not yet implemented")
        }
    }

    @SubscribeEvent
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory.holder != GlobalHolder) return
        event.currentItem?.let { SimpleNuiElement.fromItem(it) }
            ?.onClick()
            ?.let { it(event) }
    }

    @SubscribeEvent
    fun onInventoryDrag(event: InventoryDragEvent) {
        if (event.inventory.holder != GlobalHolder) return
        event.oldCursor.let { SimpleNuiElement.fromItem(it) }
            ?.onDrag()
            ?.let { it(event) }
    }
}