package taboolib.module.ui.type

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.ClickType
import taboolib.module.ui.Menu
import taboolib.module.ui.buildMenu
import taboolib.platform.util.isNotAir

@Isolated
class Stored(title: String) : Menu(title) {

    private var rows = 1
    private var handLocked = true
    private var onClick: ((event: ClickEvent) -> Unit) = {}
    private var onClose: ((event: InventoryCloseEvent) -> Unit) = {}
    private var onBuild: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }
    private var onBuildAsync: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }
    private val rule = Rule()

    fun rows(rows: Int) {
        this.rows = rows
    }

    fun handLocked(handLocked: Boolean) {
        this.handLocked = handLocked
    }

    fun onClick(onClick: (event: ClickEvent) -> Unit) {
        this.onClick = onClick
    }

    fun onClose(onClose: (event: InventoryCloseEvent) -> Unit) {
        this.onClose = onClose
    }

    fun onBuild(async: Boolean = false, onBuild: (player: Player, inventory: Inventory) -> Unit) {
        if (async) {
            this.onBuildAsync = onBuild
        } else {
            this.onBuild = onBuild
        }
    }

    fun rule(rule: Rule.() -> Unit) {
        rule(this.rule)
    }

    override fun build(): Inventory {
        return buildMenu<Basic>(title) {
            handLocked(handLocked)
            rows(rows)
            onClick {
                if (it.clickType === ClickType.DRAG) {
                    it.dragEvent().rawSlots.forEach { slot ->
                        if (slot < it.dragEvent().inventory.size) {
                            it.isCancelled = true
                        }
                    }
                }
                if (it.clickType === ClickType.CLICK) {
                    val currentItem = it.currentItem
                    // 自动装填
                    if (it.clickEvent().click.isShiftClick && it.rawSlot >= it.inventory.size && currentItem.isNotAir()) {
                        it.isCancelled = true
                        // 获取有效位置
                        val firstSlot = rule.firstSlot(it.inventory, currentItem!!)
                        if (firstSlot >= 0) {
                            // 设置物品
                            rule.writeItem(it.inventory, currentItem, firstSlot)
                            // 移除物品
                            it.currentItem = null
                            onClick(it)
                        }
                    } else {
                        if (it.clickEvent().action == InventoryAction.COLLECT_TO_CURSOR) {
                            it.isCancelled = true
                            return@onClick
                        }
                        val action = when {
                            it.clickEvent().click.isShiftClick && it.rawSlot >= 0 && it.rawSlot < it.inventory.size -> ActionQuickTake()
                            it.clickEvent().click == org.bukkit.event.inventory.ClickType.NUMBER_KEY -> ActionKeyboard()
                            else -> ActionClick()
                        }
                        val cursor = action.getCursor(it) ?: ItemStack(Material.AIR)
                        // 点击有效位置
                        if (rule.checkSlot(it.inventory, cursor, action.getCurrentSlot(it))) {
                            it.isCancelled = true
                            // 提取物品
                            action.setCursor(it, rule.readItem(it.inventory, action.getCurrentSlot(it)))
                            // 写入物品
                            rule.writeItem(it.inventory, cursor, action.getCurrentSlot(it))
                            onClick(it)
                        } else if (it.rawSlot >= 0 && it.rawSlot < it.inventory.size) {
                            it.isCancelled = true
                        }
                    }
                }
            }
            onBuild { player, inventory ->
                onBuild(player, inventory)
            }
            onBuild(true) { player, inventory ->
                onBuildAsync(player, inventory)
            }
            onClose {
                onClose(it)
            }
        }
    }

    class Rule {

        internal var checkSlot: ((inventory: Inventory, itemStack: ItemStack, slot: Int) -> Boolean) = { _, _, _ -> false }
        internal var firstSlot: ((inventory: Inventory, itemStack: ItemStack) -> Int) = { _, _ -> -1 }
        internal var writeItem: ((inventory: Inventory, itemStack: ItemStack?, slot: Int) -> Unit) = { inventory, item, slot -> inventory.setItem(slot, item) }
        internal var readItem: ((inventory: Inventory, slot: Int) -> ItemStack?) = { inventory, slot -> inventory.getItem(slot) }

        fun checkSlot(checkSlot: (inventory: Inventory, itemStack: ItemStack, slot: Int) -> Boolean) {
            this.checkSlot = checkSlot
        }

        fun firstSlot(firstSlot: (inventory: Inventory, itemStack: ItemStack) -> Int) {
            this.firstSlot = firstSlot
        }

        fun writeItem(writeItem: (inventory: Inventory, itemStack: ItemStack?, slot: Int) -> Unit) {
            this.writeItem = writeItem
        }

        fun readItem(readItem: (inventory: Inventory, slot: Int) -> ItemStack?) {
            this.readItem = readItem
        }
    }
}