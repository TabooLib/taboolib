package taboolib.module.ui.type

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.*
import taboolib.platform.util.ItemBuilder
import taboolib.platform.util.buildItem
import taboolib.platform.util.isNotAir

@Isolated
open class Stored(title: String) : Menu(title) {

    private var rows = 1
    private var handLocked = true
    private var items = HashMap<Char, ItemStack>()
    private var slots = ArrayList<List<Char>>()
    private val onClick = ArrayList<(ClickEvent) -> Unit>()
    private var onClose: ((event: InventoryCloseEvent) -> Unit) = {}
    private var onBuild: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }
    private var onBuildAsync: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }
    private var holder: ((menu: Basic) -> MenuHolder) = { MenuHolder(it) }

    private val rule = Rule()

    fun rows(rows: Int) {
        this.rows = rows
    }

    fun handLocked(handLocked: Boolean) {
        this.handLocked = handLocked
    }

    fun holder(func: (menu: Basic) -> MenuHolder) {
        this.holder = func
    }

    fun onClick(onClick: (event: ClickEvent) -> Unit) {
        this.onClick += onClick
    }

    fun onClose(onClose: (event: InventoryCloseEvent) -> Unit) {
        this.onClose = onClose
    }

    fun onBuild(async: Boolean = false, onBuild: (player: Player, inventory: Inventory) -> Unit) {
        if (async) {
            val e = this.onBuildAsync
            this.onBuildAsync = { player, inventory ->
                onBuild(player, inventory)
                e(player, inventory)
            }
        } else {
            val e = this.onBuild
            this.onBuild = { player, inventory ->
                onBuild(player, inventory)
                e(player, inventory)
            }
        }
    }

    fun onClick(bind: Int, onClick: (event: ClickEvent) -> Unit) {
        onClick {
            if (it.rawSlot == bind) {
                onClick(it)
            }
        }
    }

    fun onClick(bind: Char, onClick: (event: ClickEvent) -> Unit) {
        onClick {
            if (it.slot == bind) {
                onClick(it)
            }
        }
    }

    fun onClick(lock: Boolean = false, onClick: (event: ClickEvent) -> Unit) {
        if (lock) {
            this.onClick += {
                it.isCancelled = true
                onClick(it)
            }
        } else {
            this.onClick += onClick
        }
    }

    fun map(vararg slots: String) {
        this.slots.clear()
        this.slots.addAll(slots.map { it.toCharArray().toList() })
    }

    fun set(slot: Char, itemStack: ItemStack) {
        items[slot] = itemStack
    }

    fun set(slot: Char, material: XMaterial, itemBuilder: ItemBuilder.() -> Unit = {}) {
        set(slot, buildItem(material, itemBuilder))
    }

    fun set(slot: Int, material: XMaterial, itemBuilder: ItemBuilder.() -> Unit = {}) {
        set(slot, buildItem(material, itemBuilder))
    }

    fun set(slot: Int, itemStack: ItemStack) {
        onBuild { _, it ->
            it.setItem(slot, itemStack)
        }
    }

    fun rule(rule: Rule.() -> Unit) {
        rule(this.rule)
    }

    override fun build(): Inventory {
        return buildMenu<Basic>(title) {
            holder(this@Stored.holder)
            handLocked(this@Stored.handLocked)
            rows(this@Stored.rows)
            map(*this@Stored.slots.map { it.joinToString("") }.toTypedArray())
            onClick {
                if (it.clickType === ClickType.DRAG) {
                    it.dragEvent().rawSlots.forEach { slot ->
                        if (slot < it.dragEvent().inventory.size) {
                            it.isCancelled = true
                        }
                    }
                }
                if (it.clickType === ClickType.CLICK) {
                    this@Stored.onClick.forEach { click -> click(it) }
                    if (it.isCancelled) {
                        return@onClick
                    }
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
                        } else if (it.rawSlot >= 0 && it.rawSlot < it.inventory.size) {
                            it.isCancelled = true
                        }
                    }
                }
            }
            onBuild { player, inventory ->
                var row = 0
                while (row < this@Stored.slots.size) {
                    val line = this@Stored.slots[row]
                    var cel = 0
                    while (cel < line.size && cel < 9) {
                        inventory.setItem(row * 9 + cel, this@Stored.items[line[cel]] ?: ItemStack(Material.AIR))
                        cel++
                    }
                    row++
                }
                this@Stored.onBuild(player, inventory)
            }
            onBuild(true) { player, inventory ->
                this@Stored.onBuildAsync(player, inventory)
            }
            onClose {
                this@Stored.onClose(it)
            }
        }
    }

    class Rule {

        internal var checkSlot: ((inventory: Inventory, itemStack: ItemStack, slot: Int) -> Boolean) = { _, _, _ -> false }
        internal var firstSlot: ((inventory: Inventory, itemStack: ItemStack) -> Int) = { _, _ -> -1 }
        internal var writeItem: ((inventory: Inventory, itemStack: ItemStack, slot: Int) -> Unit) = { inventory, item, slot -> inventory.setItem(slot, item) }
        internal var readItem: ((inventory: Inventory, slot: Int) -> ItemStack?) = { inventory, slot -> inventory.getItem(slot) }

        fun checkSlot(intRange: Int, checkSlot: (inventory: Inventory, itemStack: ItemStack) -> Boolean) {
            checkSlot(intRange..intRange, checkSlot)
        }

        fun checkSlot(intRange: IntRange, checkSlot: (inventory: Inventory, itemStack: ItemStack) -> Boolean) {
            val e = this.checkSlot
            this.checkSlot = { inventory, itemStack, slot ->
                if (slot in intRange) {
                    checkSlot(inventory, itemStack)
                } else {
                    e(inventory, itemStack, slot)
                }
            }
        }

        fun checkSlot(checkSlot: (inventory: Inventory, itemStack: ItemStack, slot: Int) -> Boolean) {
            this.checkSlot = checkSlot
        }

        fun firstSlot(firstSlot: (inventory: Inventory, itemStack: ItemStack) -> Int) {
            this.firstSlot = firstSlot
        }

        fun writeItem(writeItem: (inventory: Inventory, itemStack: ItemStack, slot: Int) -> Unit) {
            this.writeItem = writeItem
        }

        fun readItem(readItem: (inventory: Inventory, slot: Int) -> ItemStack?) {
            this.readItem = readItem
        }
    }
}