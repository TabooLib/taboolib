package taboolib.module.ui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

abstract class Menu(val title: String) {

    abstract fun build(): Inventory

    open fun onOpen(player: Player, inventory: Inventory) {}
}