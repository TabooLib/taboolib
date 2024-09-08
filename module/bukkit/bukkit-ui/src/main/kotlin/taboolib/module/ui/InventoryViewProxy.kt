package taboolib.module.ui

import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import taboolib.common.util.unsafeLazy
import taboolib.module.ui.util.InventoryViewInterface

/**
 * TabooLib
 * taboolib.module.ui.util.InventoryView
 *
 * @author mical
 * @since 2024/8/11 23:03
 */
object InventoryViewProxy {

    private val isInterfaceInventoryView by unsafeLazy {
        InventoryView::class.java.isInterface
    }

    // 此方法不生效
//    fun HumanEntity.getFixedOpenInventory(): InventoryView {
//        return if (isInterfaceInventoryView) InventoryViewInterface.getOpenInventory(this) else openInventory
//    }
//
//    fun InventoryEvent.getFixedView(): InventoryView {
//        return if (isInterfaceInventoryView) InventoryViewInterface.getView(this) else view
//    }

    fun getTopInventory(view: InventoryView): Inventory {
        if (isInterfaceInventoryView) {
            return InventoryViewInterface.getTopInventory(view)
        }
        return view.topInventory
    }

    fun getBottomInventory(view: InventoryView): Inventory {
        if (isInterfaceInventoryView) {
            return InventoryViewInterface.getBottomInventory(view)
        }
        return view.bottomInventory
    }

    fun getCursor(view: InventoryView): ItemStack? {
        if (isInterfaceInventoryView) {
            return InventoryViewInterface.getCursor(view)
        }
        return view.cursor
    }

    fun setCursor(view: InventoryView, item: ItemStack?) {
        if (isInterfaceInventoryView) {
            InventoryViewInterface.setCursor(view, item)
        }
        view.cursor = item
    }

    fun getInventory(view: InventoryView, slot: Int): Inventory? {
        if (isInterfaceInventoryView) {
            return InventoryViewInterface.getInventory(view, slot)
        }
        return view.getInventory(slot)
    }

    fun getSlotType(view: InventoryView, slot: Int): InventoryType.SlotType {
        if (isInterfaceInventoryView) {
            return InventoryViewInterface.getSlotType(view, slot)
        }
        return view.getSlotType(slot)
    }

    fun getTitle(view: InventoryView): String {
        if (isInterfaceInventoryView) {
            return InventoryViewInterface.getTitle(view)
        }
        return view.title
    }
}