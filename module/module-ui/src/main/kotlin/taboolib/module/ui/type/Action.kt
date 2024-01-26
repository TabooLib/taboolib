package taboolib.module.ui.type

import org.bukkit.inventory.ItemStack
import taboolib.module.ui.ClickEvent

abstract class Action {

    abstract fun getCursor(e: ClickEvent): ItemStack?

    abstract fun setCursor(e: ClickEvent, item: ItemStack?)

    abstract fun getCurrentSlot(e: ClickEvent): Int
}