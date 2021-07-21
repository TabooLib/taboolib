package taboolib.module.ui.type

import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated
import taboolib.module.ui.ClickEvent

@Isolated
abstract class Action {

    abstract fun getCurrent(e: ClickEvent): ItemStack

    abstract fun setCurrent(e: ClickEvent, item: ItemStack?)

    abstract fun getCurrentSlot(e: ClickEvent): Int
}