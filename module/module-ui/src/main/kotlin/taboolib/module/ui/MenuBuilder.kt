package taboolib.module.ui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.module.ui.virtual.VirtualInventory
import taboolib.module.ui.virtual.inject
import taboolib.module.ui.virtual.openVirtualInventory
import taboolib.platform.util.isNotAir

inline fun <reified T : Menu> buildMenu(title: String = "chest", builder: T.() -> Unit): Inventory {
    return T::class.java.getDeclaredConstructor(String::class.java).newInstance(title).also(builder).build()
}

inline fun <reified T : Menu> Player.openMenu(title: String = "chest", builder: T.() -> Unit) {
    try {
        val buildMenu = buildMenu(title, builder)
        if (buildMenu is VirtualInventory) {
            val remoteInventory = openVirtualInventory(buildMenu)
            val basic = MenuHolder.fromInventory(buildMenu)
            if (basic != null) {
                remoteInventory.inject(basic)
            }
        } else {
            openInventory(buildMenu)
        }
    } catch (ex: Throwable) {
        ex.printStackTrace()
    }
}

/**
 * 获取当前点击事件下所有受影响的物品
 */
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