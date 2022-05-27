package taboolib.module.ui.nextgen.internal

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getItemTag
import taboolib.module.nms.setItemTag
import taboolib.module.ui.nextgen.api.NuiElement
import java.util.*

internal data class SimpleNuiElement(
    var item: ItemStack = ItemStack(Material.AIR),
    internal var context: NuiElementContext = NuiElementContext({}, {})
) : NuiElement {

    override fun item() = item
    override fun item(item: ItemStack): NuiElement {
        this.item = item
        return this
    }
    override fun onClick() = context.onClick
    override fun onClick(block: (InventoryClickEvent) -> Unit): NuiElement {
        context.onClick = block
        return this
    }
    override fun onDrag() = context.onDrag
    override fun onDrag(block: (InventoryDragEvent) -> Unit): NuiElement {
        context.onDrag = block
        return this
    }

    internal fun initializedItem(): ItemStack {
        val uuid = UUID.randomUUID()
        val tag = item.clone().getItemTag().apply {
            put(ID_KEY, uuid.toString())
        }
        ctxReferenceMap[uuid] = context

        return item.clone().setItemTag(tag)
    }

    companion object {
        private val ctxReferenceMap = mutableMapOf<UUID, NuiElementContext>()
        private const val ID_KEY = "NuiElementID"

        fun fromItem(item: ItemStack): SimpleNuiElement? {
            val tag = item.getItemTag()
            Bukkit.broadcastMessage(tag.asString())
            if (!tag.containsKey(ID_KEY)) return null
            val ctx = ctxReferenceMap[UUID.fromString(tag[ID_KEY]!!.asString())]!!
            return SimpleNuiElement(clearNuiID(item), ctx)
        }

        private fun clearNuiID(item: ItemStack): ItemStack {
            val tag = item.clone().getItemTag().apply {
                remove(ID_KEY)
            }
            return item.clone().setItemTag(tag)
        }
    }
}
