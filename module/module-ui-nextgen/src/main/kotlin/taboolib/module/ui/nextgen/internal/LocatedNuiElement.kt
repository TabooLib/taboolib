package taboolib.module.ui.nextgen.internal

import org.bukkit.inventory.Inventory
import taboolib.module.ui.nextgen.api.NuiElement

data class LocatedNuiElement(
    val rows: Int,
    val columns: Int,
    val element: NuiElement
) {
    fun applyTo(inventory: Inventory) {
        inventory.setItem(
            ((rows - 1) * 9) + (columns - 1),
            (element as SimpleNuiElement).initializedItem()
        )
    }
}
