package taboolib.module.ui.nextgen.internal

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.nextgen.api.NextgenUI
import taboolib.module.ui.nextgen.api.NuiElement

class SimpleNextgenUI : NextgenUI {
    private val elements = mutableSetOf<LocatedNuiElement>()
    private lateinit var viewer: Player
    private var rows: Int? = null
    private var title: String? = null

    override fun viewer(player: Player) {
        viewer = player
    }

    override fun rows(rows: Int) {
        this.rows = rows
    }

    override fun title(title: String) {
        this.title = title
    }

    override fun addElement(rows: Int, columns: Int, element: NuiElement) {
        elements += LocatedNuiElement(rows, columns, element)
        render()
    }

    override fun removeElement(element: NuiElement) {
        val result = elements.removeIf { it.element == element }
        if (result) render()
    }

    override fun removeElementAt(rows: Int, columns: Int) {
        val result = elements.removeIf { it.rows == rows && it.columns == columns }
        if (result) render()
    }

    override fun removeAllBy(item: ItemStack) {
        val result = elements.removeIf { it.element.item() == item }
        if (result) render()
    }

    override fun render() {
        val inventory = when {
            rows == null && title == null -> {
                val size = elements.maxOfOrNull { it.rows * 9 + 9 } ?: 9
                Bukkit.createInventory(NuiListener.GlobalHolder, size)
            }
            rows == null && title != null -> {
                val size = elements.maxOfOrNull { it.rows * 9 + 9 } ?: 9
                Bukkit.createInventory(NuiListener.GlobalHolder, size, title!!)
            }
            rows != null && title == null -> {
                Bukkit.createInventory(NuiListener.GlobalHolder, rows!! * 9)
            }
            else -> {
                Bukkit.createInventory(NuiListener.GlobalHolder, rows!! * 9, title!!)
            }
        }
        for (element in elements) {
            inventory.setItem(
                ((element.rows - 1) * 9) + (element.columns - 1),
                (element.element as SimpleNuiElement).initializedItem()
            )
        }

        viewer.openInventory(inventory)
    }
}
