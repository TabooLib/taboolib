package taboolib.module.ui.type

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * 可储存容器
 */
interface StorableChest : Chest {

    /**
     * 定义页面规则
     */
    fun rule(rule: Rule.() -> Unit)

    /**
     * 页面规则
     */
    interface Rule {

        /**
         * 定义判定位置
         * 玩家是否可以将物品放入
         */
        fun checkSlot(intRange: Int, checkSlot: (inventory: Inventory, itemStack: ItemStack) -> Boolean)

        /**
         * 定义判定位置
         * 玩家是否可以将物品放入
         */
        fun checkSlot(intRange: IntRange, callback: (inventory: Inventory, itemStack: ItemStack) -> Boolean)

        /**
         * 定义判定位置
         * 玩家是否可以将物品放入
         */
        fun checkSlot(callback: (inventory: Inventory, itemStack: ItemStack, slot: Int) -> Boolean)

        /**
         * 获取页面中首个有效的位置
         * 用于玩家 SHIFT 点击快速放入物品，不再触发 checkSlot 回调
         */
        fun firstSlot(firstSlot: (inventory: Inventory, itemStack: ItemStack) -> Int)

        /**
         * 物品写入回调
         */
        fun writeItem(writeItem: (inventory: Inventory, itemStack: ItemStack, slot: Int) -> Unit)

        /**
         * 物品写入回调
         */
        fun writeItem(writeItem: (inventory: Inventory, itemStack: ItemStack, slot: Int, type: BukkitClickType) -> Unit)

        /**
         * 读取物品回调
         */
        fun readItem(readItem: (inventory: Inventory, slot: Int) -> ItemStack?)
    }
}

typealias BukkitClickType = org.bukkit.event.inventory.ClickType