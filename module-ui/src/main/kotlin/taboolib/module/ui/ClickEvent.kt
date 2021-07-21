package taboolib.module.ui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * @author 坏黑
 * @since 2019-05-21 18:09
 */
class ClickEvent(private val bukkitEvent: InventoryInteractEvent, val clickType: ClickType, val slot: Char) {

    val rawSlot: Int
        get() = if (clickType === ClickType.CLICK) clickEvent().rawSlot else -1

    val clicker: Player
        get() = bukkitEvent.whoClicked as Player

    val inventory: Inventory
        get() = bukkitEvent.inventory

    val affectItems: List<ItemStack>
        get() = if (clickType === ClickType.CLICK) clickEvent().getAffectItems() else emptyList()

    var isCancelled: Boolean
        get() = bukkitEvent.isCancelled
        set(isCancelled) {
            bukkitEvent.isCancelled = isCancelled
        }

    var currentItem: ItemStack?
        get() = if (clickType === ClickType.CLICK) clickEvent().currentItem else null
        set(item) {
            if (clickType === ClickType.CLICK) {
                clickEvent().currentItem = item
            }
        }

    fun onClick(consumer: InventoryClickEvent.() -> Unit): ClickEvent {
        consumer(clickEvent())
        return this
    }

    fun onDrag(consumer: InventoryDragEvent.() -> Unit): ClickEvent {
        consumer(dragEvent())
        return this
    }

    fun clickEvent(): InventoryClickEvent {
        return bukkitEvent as InventoryClickEvent
    }

    fun dragEvent(): InventoryDragEvent {
        return bukkitEvent as InventoryDragEvent
    }
}