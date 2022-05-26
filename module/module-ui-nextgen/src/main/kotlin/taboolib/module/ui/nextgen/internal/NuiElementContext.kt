package taboolib.module.ui.nextgen.internal

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

internal data class NuiElementContext(
    var onClick: (InventoryClickEvent) -> Unit,
    var onDrag: (InventoryDragEvent) -> Unit,
)
