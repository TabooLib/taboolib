@file:Isolated
package taboolib.module.ui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated
import taboolib.platform.util.isNotAir
import kotlin.collections.ArrayList

inline fun <reified T : Menu> buildMenu(title: String = "chest", builder: T.() -> Unit): Inventory {
    return T::class.java.getDeclaredConstructor(String::class.java).newInstance(title).also(builder).build()
}

inline fun <reified T : Menu> Player.openMenu(title: String = "chest", builder: T.() -> Unit) {
    openInventory(buildMenu(title, builder))
}

fun InventoryClickEvent.getAffectItems(): List<ItemStack> {
    val items = ArrayList<ItemStack>()
    if (click == ClickType.NUMBER_KEY) {
        val hotbarButton = whoClicked.inventory.getItem(hotbarButton)
        if (hotbarButton.isNotAir()) {
            items += hotbarButton!!
        }
    }
    if (currentItem.isNotAir()) {
        items += currentItem!!
    }
    return items
}