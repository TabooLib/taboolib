package taboolib.module.ui.virtual

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * TabooLib
 * taboolib.module.ui.virtual.RemoteInventoryLegacy
 *
 * @author 坏黑
 * @since 2023/3/20 17:48
 */
interface RemoteInventoryModern {

    fun inventory(): Inventory

    fun bottomInventory(): Inventory

    fun viewer(): Player

    fun title(): String
}