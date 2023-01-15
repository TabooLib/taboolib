package taboolib.module.ui.type

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.virtual.virtualize

open class Hopper(title: String = "chest") : Basic(title) {

    override fun build(): Inventory {
        var inventory = Bukkit.createInventory(holderCallback(this), InventoryType.HOPPER, title)
        if (virtual) {
            inventory = inventory.virtualize()
        }
        val line = slots[0]
        var cel = 0
        while (cel < line.size && cel < 5) {
            inventory.setItem(cel, items[line[cel]] ?: ItemStack(Material.AIR))
            cel++
        }
        return inventory
    }
}