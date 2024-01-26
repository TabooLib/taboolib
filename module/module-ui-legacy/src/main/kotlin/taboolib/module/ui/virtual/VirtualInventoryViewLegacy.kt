package taboolib.module.ui.virtual

import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView

/**
 * TabooLib
 * taboolib.module.ui.virtual.VirtualInventoryViewLegacy
 *
 * @author 坏黑
 * @since 2023/1/16 04:11
 */
class VirtualInventoryViewLegacy(val remoteInventory: RemoteInventoryLegacy) : InventoryView() {

    override fun getTopInventory(): Inventory {
        return remoteInventory.inventory()
    }

    override fun getBottomInventory(): Inventory {
        return remoteInventory.bottomInventory()
    }

    override fun getPlayer(): HumanEntity {
        return remoteInventory.viewer()
    }

    override fun getType(): InventoryType {
        return remoteInventory.inventory().type
    }
}