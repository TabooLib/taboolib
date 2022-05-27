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
    private var inited = false

    override fun viewer(player: Player) {
        viewer = player
    }

    override fun rows(rows: Int) {
        this.rows = rows
    }

    override fun title(title: String) {
        this.title = title
    }

    fun inited() {
        inited = true
    }

    override fun addElement(rows: Int, columns: Int, element: NuiElement) {
        elements += LocatedNuiElement(rows, columns, element)
        if (inited) render()
    }

    override fun removeElement(element: NuiElement) {
        val result = elements.removeIf { it.element == element }
        if (inited && result) render()
    }

    override fun removeElementAt(rows: Int, columns: Int) {
        val result = elements.removeIf { it.rows == rows && it.columns == columns }
        if (inited && result) render()
    }

    override fun removeAllBy(item: ItemStack) {
        val result = elements.removeIf { it.element.item() == item }
        if (inited && result) render()
    }

    override fun render() {
        val predictSize = elements.maxOfOrNull { it.rows * 9 + 9 } ?: 9
        val inventory = when {
            rows == null && title == null ->
                Bukkit.createInventory(NuiListener.GlobalHolder, predictSize)
            rows == null && title != null ->
                Bukkit.createInventory(NuiListener.GlobalHolder, predictSize, title!!)
            rows != null && title == null ->
                Bukkit.createInventory(NuiListener.GlobalHolder, rows!! * 9)
            else ->
                Bukkit.createInventory(NuiListener.GlobalHolder, rows!! * 9, title!!)
        }
        for (element in elements) {
            element.applyTo(inventory)
        }

        viewer.openInventory(inventory)
    }
}
