package taboolib.module.ui

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.type.Chest
import taboolib.module.ui.virtual.RemoteInventory
import taboolib.module.ui.virtual.VirtualInventoryInteractEvent

/**
 * @author 坏黑
 * @since 2019-05-21 18:09
 */
class ClickEvent(private val bukkitEvent: InventoryInteractEvent, val clickType: ClickType, val slot: Char, val builder: Chest) {

    val clicker: Player
        get() = bukkitEvent.whoClicked as Player

    val inventory: Inventory
        get() = bukkitEvent.inventory

    /** 影响物品 */
    val affectItems: List<ItemStack>
        get() = if (clickType === ClickType.CLICK) clickEvent().getAffectItems() else emptyList()

    /** 取消事件 */
    var isCancelled: Boolean
        get() = bukkitEvent.isCancelled
        set(isCancelled) {
            bukkitEvent.isCancelled = isCancelled
        }

    /** 点击位置 */
    val rawSlot: Int
        get() = when (clickType) {
            ClickType.CLICK -> clickEvent().rawSlot
            ClickType.VIRTUAL -> virtualEvent().clickSlot
            else -> -1
        }

    /** 键盘按键 */
    val hotbarKey: Int
        get() = when (clickType) {
            ClickType.CLICK -> clickEvent().hotbarButton
            ClickType.VIRTUAL -> virtualEvent().hotbarKey
            else -> -1
        }

    /** 获取或设置点击物品 */
    var currentItem: ItemStack?
        get() = when (clickType) {
            ClickType.CLICK -> clickEvent().currentItem
            ClickType.VIRTUAL -> virtualEvent().clickItem
            else -> null
        }
        set(item) {
            when (clickType) {
                ClickType.CLICK -> {
                    clickEvent().currentItem = item
                }
                ClickType.VIRTUAL -> {
                    inventory.setItem(virtualEvent().clickSlot, item)
                }
                else -> {}
            }
        }

    /** 获取物品 */
    fun getItem(slot: Char): ItemStack? {
        val idx = builder.slots.flatten().indexOf(slot)
        return if (idx in 0 until inventory.size) inventory.getItem(idx) else null
    }

    /** 获取物品列表 */
    fun getItems(slot: Char): List<ItemStack> {
        return builder.slots.flatten().mapIndexedNotNull { index, c -> if (c == slot) inventory.getItem(index) ?: ItemStack(Material.AIR) else null }
    }

    /** 转换为点击事件 */
    fun clickEvent(): InventoryClickEvent {
        if (clickType != ClickType.CLICK) {
            error("clickEvent() is not available in \"$clickType\" action")
        }
        return bukkitEvent as InventoryClickEvent
    }

    /** 安全转换为点击事件 */
    fun clickEventOrNull(): InventoryClickEvent? {
        return bukkitEvent as? InventoryClickEvent
    }

    /** 转换为拖拽事件 */
    fun dragEvent(): InventoryDragEvent {
        if (clickType != ClickType.DRAG) {
            error("dragEvent() is not available in \"$clickType\" action.")
        }
        return bukkitEvent as InventoryDragEvent
    }

    /** 安全转换为拖拽事件 */
    fun dragEventOrNull(): InventoryDragEvent? {
        return bukkitEvent as? InventoryDragEvent
    }

    /** 转换为虚拟点击事件 */
    fun virtualEvent(): RemoteInventory.ClickEvent {
        if (clickType != ClickType.VIRTUAL) {
            error("virtualEvent() is not available in \"$clickType\" action.")
        }
        return (bukkitEvent as VirtualInventoryInteractEvent).clickEvent
    }

    /** 安全转换为虚拟点击事件 */
    fun virtualEventOrNull(): RemoteInventory.ClickEvent? {
        return (bukkitEvent as? VirtualInventoryInteractEvent)?.clickEvent
    }

    /** 用安全的方式处理点击事件 */
    fun onClick(consumer: InventoryClickEvent.() -> Unit): ClickEvent {
        if (clickType == ClickType.CLICK) {
            consumer(clickEvent())
        }
        return this
    }

    /** 用安全的方式处理拖拽事件 */
    fun onDrag(consumer: InventoryDragEvent.() -> Unit): ClickEvent {
        if (clickType == ClickType.DRAG) {
            consumer(dragEvent())
        }
        return this
    }

    /** 用安全的方式处理虚拟点击事件 */
    fun onVirtualClick(consumer: RemoteInventory.ClickEvent.() -> Unit): ClickEvent {
        if (clickType == ClickType.VIRTUAL) {
            consumer(virtualEvent())
        }
        return this
    }
}