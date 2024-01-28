package taboolib.module.ui.type

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * 铁砧容器
 */
interface Anvil : Chest {

    /** 当物品被重命名时 */
    fun onRename(callback: (Player, String, Inventory) -> Unit)
}