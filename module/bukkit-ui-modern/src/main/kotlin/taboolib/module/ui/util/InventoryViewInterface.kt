package taboolib.module.ui.util

import org.bukkit.event.inventory.InventoryType.SlotType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * TabooLib
 * taboolib.module.ui.util.InventoryViewInterface
 *
 * @author mical
 * @since 2024/8/11 23:11
 */
object InventoryViewInterface {

    // 此方法不生效
//    fun getOpenInventory(entity: HumanEntity): InventoryView {
//        return entity.openInventory
//    }
//
//    fun getView(event: InventoryEvent): InventoryView {
//        return event.view
//    }

    fun getTopInventory(view: InventoryView): Inventory {
        return view.topInventory
    }

    fun getBottomInventory(view: InventoryView): Inventory {
        return view.bottomInventory
    }

    fun getCursor(view: InventoryView): ItemStack? {
        return view.cursor
    }

    fun setCursor(view: InventoryView, item: ItemStack?) {
        view.cursor = item
    }

    fun getInventory(view: InventoryView, slot: Int): Inventory? {
        return view.getInventory(slot)
    }

    fun getSlotType(view: InventoryView, slot: Int): SlotType {
        return view.getSlotType(slot)
    }

    fun getTitle(view: InventoryView): String {
        return view.title
    }
}