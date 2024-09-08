package taboolib.module.ui.type

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * TabooLib
 * taboolib.module.ui.type.AnvilCallback
 *
 * @author 坏黑
 * @since 2024/1/28 18:28
 */
interface AnvilCallback {

    fun invoke(player: Player, text: String, inventory: Inventory)
}