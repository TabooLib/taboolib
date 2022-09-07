package taboolib.module.ui.type

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

open class Hopper(title: String = "chest") : Basic(title) {

    override fun build(): Inventory {
        val inventory = Bukkit.createInventory(holderCallback(this), InventoryType.HOPPER, title)
        val line = slots[0]
        var cel = 0
        while (cel < line.size && cel < 5) {
            inventory.setItem(cel, items[line[cel]] ?: ItemStack(Material.AIR))
            cel++
        }
        return inventory
    }
}