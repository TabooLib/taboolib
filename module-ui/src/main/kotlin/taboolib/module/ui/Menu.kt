package taboolib.module.ui

import org.bukkit.inventory.Inventory

abstract class Menu(val title: String) {

    internal abstract fun build(): Inventory
}