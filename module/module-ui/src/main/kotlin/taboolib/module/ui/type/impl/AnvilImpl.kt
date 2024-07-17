package taboolib.module.ui.type.impl

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.type.Anvil
import taboolib.module.ui.type.AnvilCallback
import taboolib.module.ui.virtual.virtualize

/**
 * FlexUI
 * me.asgard.frontier.flexui.menu.FAnvil
 *
 * @author arasple
 * @since 2023/11/18 16:50
 */
open class AnvilImpl(title: String) : ChestImpl(title), Anvil, AnvilCallback {

    internal var renameCallback: ((Player, String, Inventory) -> Unit)? = null

    /** 当物品被重命名时 */
    override fun onRename(callback: (Player, String, Inventory) -> Unit) {
        renameCallback = callback
    }

    override fun invoke(player: Player, text: String, inventory: Inventory) {
        renameCallback?.invoke(player, text, inventory)
    }

    override fun build(): Inventory {
        var inventory = Bukkit.createInventory(holderCallback(this), InventoryType.ANVIL, title)
        if (virtualized) {
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