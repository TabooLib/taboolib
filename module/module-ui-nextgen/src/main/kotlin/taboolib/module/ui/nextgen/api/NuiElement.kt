package taboolib.module.ui.nextgen.api

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.nextgen.internal.SimpleNuiElement

interface NuiElement {
    fun item(): ItemStack
    fun item(item: ItemStack): NuiElement

    fun onClick(block: (InventoryClickEvent) -> Unit): NuiElement
    fun onClick(): (InventoryClickEvent) -> Unit

    fun onDrag(block: (InventoryDragEvent) -> Unit): NuiElement
    fun onDrag(): (InventoryDragEvent) -> Unit

    companion object Factory {
        fun item(item: ItemStack): NuiElement {
            SimpleNuiElement.fromItem(item)?.let { return it }
            return SimpleNuiElement().apply { item(item) }
        }
    }
}
