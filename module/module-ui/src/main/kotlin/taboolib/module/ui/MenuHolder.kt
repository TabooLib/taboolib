package taboolib.module.ui

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import taboolib.module.ui.type.Basic

/**
 * @author 坏黑
 * @since 2019-05-21 20:28
 */
open class MenuHolder(val menu: Basic) : InventoryHolder {

    private val inventory = Bukkit.createInventory(this, if (menu.rows > 0) menu.rows * 9 else menu.slots.size * 9, menu.title)

    override fun getInventory(): Inventory {
        return inventory
    }

    companion object {

        fun fromInventory(inventory: Inventory): Basic? {
            return (inventory.holder as? MenuHolder)?.menu
        }
    }
}