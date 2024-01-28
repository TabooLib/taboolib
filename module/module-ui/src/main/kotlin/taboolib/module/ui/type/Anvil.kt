package taboolib.module.ui.type

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.MenuHolder
import taboolib.module.ui.type.Basic
import taboolib.module.ui.virtual.virtualize

/**
 * FlexUI
 * me.asgard.frontier.flexui.menu.FAnvil
 *
 * @author arasple
 * @since 2023/11/18 16:50
 */
open class Anvil(title: String = "...") : Basic(title) {

    internal var renameCallback: ((Player, String, Inventory) -> Unit)? = null

    /** 当物品被重命名时 */
    fun onRename(callback: (Player, String, Inventory) -> Unit) {
        renameCallback = callback
    }

    override fun build(): Inventory {
        var inventory = Bukkit.createInventory(holderCallback(this), InventoryType.ANVIL, title)
        if (virtual) {
            inventory = inventory.virtualize()
        }
        if (slots.isNotEmpty()) {
            val line = slots[0]
            var cel = 0
            while (cel < line.size && cel < 3) {
                inventory.setItem(cel, items[line[cel]] ?: ItemStack(Material.AIR))
                cel++
            }
        }
        return inventory
    }
}