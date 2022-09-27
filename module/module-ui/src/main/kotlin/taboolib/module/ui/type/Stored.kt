package taboolib.module.ui.type

import org.bukkit.Material
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.ClickType
import taboolib.platform.util.isNotAir

@Isolated
open class Stored(title: String) : Basic(title) {

    /** 页面规则 **/
    internal val rule = Rule()

    /**
     * 定义页面规则
     */
    open fun rule(rule: Rule.() -> Unit) {
        rule(this.rule)
    }

    /**
     * 点击事件回调
     * 仅在特定位置下触发
     * 相比于 Basic，Stored 的所有点击事件回调均会处理 DRAG 类型
     */
    override fun onClick(bind: Int, callback: (event: ClickEvent) -> Unit) {
        onClick {
            if (it.rawSlot == bind) {
                callback(it)
            }
        }
    }

    /**
     * 点击事件回调
     * 仅在特定位置下触发
     */
    override fun onClick(bind: Char, callback: (event: ClickEvent) -> Unit) {
        onClick {
            if (it.slot == bind) {
                callback(it)
            }
        }
    }

    /**
     * 点击事件回调
     * 可选是否自动锁定点击位置
     */
    override fun onClick(lock: Boolean, callback: (event: ClickEvent) -> Unit) {
        if (lock) {
            clickCallback += {
                it.isCancelled = true
                callback(it)
            }
        } else {
            clickCallback += callback
        }
    }

    override fun build(): Inventory {
        // 生成点击回调
        selfClick {
            // 如果事件被取消则不再继续处理
            if (it.isCancelled) {
                return@selfClick
            }
            // 处理拖拽事件
            if (it.clickType === ClickType.DRAG) {
                // 阻止页面内的拖拽行为
                it.dragEvent().rawSlots.forEach { slot ->
                    if (slot < it.dragEvent().inventory.size) {
                        it.isCancelled = true
                    }
                }
            }
            // 处理点击事件
            else if (it.clickType === ClickType.CLICK) {
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
                    // 阻止无法处理的合并行为
                    if (it.clickEvent().action == InventoryAction.COLLECT_TO_CURSOR) {
                        it.isCancelled = true
                        return@selfClick
                    }
                    // 获取行为
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
        // 生成页面
        return super.build()
    }

    open class Rule {

        /** 检查判定位置回调 **/
        internal var checkSlot: ((inventory: Inventory, itemStack: ItemStack, slot: Int) -> Boolean) = { _, _, _ -> false }

        /** 获取可用位置回调 **/
        internal var firstSlot: ((inventory: Inventory, itemStack: ItemStack) -> Int) = { _, _ -> -1 }

        /** 写入物品回调 **/
        internal var writeItem: ((inventory: Inventory, itemStack: ItemStack, slot: Int) -> Unit) = { inventory, item, slot -> inventory.setItem(slot, item) }

        /** 读取物品回调 **/
        internal var readItem: ((inventory: Inventory, slot: Int) -> ItemStack?) = { inventory, slot -> inventory.getItem(slot) }

        /**
         * 定义判定位置
         * 玩家是否可以将物品放入
         */
        open fun checkSlot(intRange: Int, checkSlot: (inventory: Inventory, itemStack: ItemStack) -> Boolean) {
            checkSlot(intRange..intRange, checkSlot)
        }

        /**
         * 定义判定位置
         * 玩家是否可以将物品放入
         */
        open fun checkSlot(intRange: IntRange, callback: (inventory: Inventory, itemStack: ItemStack) -> Boolean) {
            val before = checkSlot
            checkSlot = { inventory, itemStack, slot ->
                if (slot in intRange) {
                    callback(inventory, itemStack)
                } else {
                    before(inventory, itemStack, slot)
                }
            }
        }

        /**
         * 定义判定位置
         * 玩家是否可以将物品放入
         */
        open fun checkSlot(checkSlot: (inventory: Inventory, itemStack: ItemStack, slot: Int) -> Boolean) {
            this.checkSlot = checkSlot
        }

        /**
         * 获取页面中首个有效的位置
         * 用于玩家 SHIFT 点击快速放入物品，不再触发 checkSlot 回调
         */
        open fun firstSlot(firstSlot: (inventory: Inventory, itemStack: ItemStack) -> Int) {
            this.firstSlot = firstSlot
        }

        /**
         * 物品写入回调
         */
        open fun writeItem(writeItem: (inventory: Inventory, itemStack: ItemStack, slot: Int) -> Unit) {
            this.writeItem = writeItem
        }

        /**
         * 读取物品回调
         */
        open fun readItem(readItem: (inventory: Inventory, slot: Int) -> ItemStack?) {
            this.readItem = readItem
        }
    }
}