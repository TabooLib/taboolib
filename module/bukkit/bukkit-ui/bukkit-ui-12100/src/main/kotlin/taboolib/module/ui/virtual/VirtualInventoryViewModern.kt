package taboolib.module.ui.virtual

import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView

/**
 * TabooLib
 * taboolib.module.ui.virtual.VirtualInventoryViewModern
 *
 * @author mical
 * @date 2024/9/8 00:08
 */
class VirtualInventoryViewModern(val remoteInventory: RemoteInventoryModern) : CraftAbstractInventoryView() {

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

    override fun getTitle(): String {
        return remoteInventory.title()
    }

    override fun getOriginalTitle(): String {
        return remoteInventory.title()
    }

    override fun setTitle(p0: String) {
    }

    companion object {

        fun newInstance(remoteInventory: RemoteInventoryModern): InventoryView {
            return VirtualInventoryViewModern(remoteInventory)
        }
    }
}