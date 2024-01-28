package taboolib.module.ui.type.impl

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.virtual.virtualize

open class HopperImpl(title: String) : ChestImpl(title) {

    override fun build(): Inventory {
        var inventory = Bukkit.createInventory(holderCallback(this), InventoryType.HOPPER, title)
        if (virtualized) {
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