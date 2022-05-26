package taboolib.module.ui.nextgen.api

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.nextgen.internal.SimpleNextgenUI

interface NextgenUI {
    fun viewer(player: Player)
    fun rows(rows: Int)
    fun title(title: String)
    fun addElement(rows: Int, columns: Int, element: NuiElement)
    fun removeElement(element: NuiElement)
    fun removeElementAt(rows: Int, columns: Int)
    fun removeAllBy(item: ItemStack)
    fun render()

    companion object {
        fun create(viewer: Player) = SimpleNextgenUI().apply { viewer(viewer) }
        fun create() = SimpleNextgenUI()
    }
}
