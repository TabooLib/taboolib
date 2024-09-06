package taboolib.platform.util

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * 检查玩家背包中的特定物品是否达到特定数量
 *
 * @param item   物品
 * @param amount 检查数量
 * @param remove 是否移除
 * @return boolean
 */
fun Player.checkItem(item: ItemStack, amount: Int = 1, remove: Boolean = false): Boolean {
    if (item.isAir()) {
        error("air")
    }
    return inventory.checkItem(item, amount, remove)
}

/**
 * 检查背包中的特定物品是否达到特定数量
 *
 * @param item      物品
 * @param amount    检查数量
 * @param remove    是否移除
 * @return boolean
 */
fun Inventory.checkItem(item: ItemStack, amount: Int = 1, remove: Boolean = false): Boolean {
    if (item.isAir()) {
        error("air")
    }
    return hasItem(amount) { it.isSimilar(item) } && (!remove || takeItem(amount) { it.isSimilar(item) })
}

/**
 * 检查背包中符合特定规则的物品是否达到特定该数量
 *
 * @param matcher   规则
 * @param amount    数量
 * @return boolean
 */
fun Inventory.hasItem(amount: Int = 1, matcher: (itemStack: ItemStack) -> Boolean): Boolean {
    var checkAmount = amount
    contents.forEach { itemStack ->
        if (itemStack.isNotAir() && matcher(itemStack)) {
            checkAmount -= itemStack.amount
            if (checkAmount <= 0) {
                return true
            }
        }
    }
    return false
}

/**
 * 移除背包中特定数量的符合特定规则的物品
 *
 * @param matcher   规则
 * @param savedItemStack 记录拿取物品的列表
 * @param amount    实例
 * @return boolean
 */
fun Inventory.takeItem(amount: Int = 1, savedItemStack:MutableList<ItemStack> = mutableListOf(), matcher: (itemStack: ItemStack) -> Boolean): Boolean {
    var takeAmount = amount
    contents.forEachIndexed { index, itemStack ->
        if (itemStack.isNotAir() && matcher(itemStack)) {
            takeAmount -= itemStack.amount
            if (takeAmount < 0) {
                savedItemStack.add(itemStack.clone().apply { this.amount = takeAmount + itemStack.amount })
                itemStack.amount -= (takeAmount + itemStack.amount)
                return savedItemStack.isNotEmpty()
            } else {
                savedItemStack.add(itemStack.clone())
                setItem(index, null)
                if (takeAmount == 0) {
                    return savedItemStack.isNotEmpty()
                }
            }
        }
    }
    return savedItemStack.isNotEmpty()
}


/**
 * 获取背包中符合特定规则的物品的数量
 *
 * @return amount
 */
fun Inventory.countItem(matcher: (itemStack: ItemStack) -> Boolean): Int {
    var amount = 0
    contents.forEach { itemStack ->
        if (itemStack.isNotAir() && matcher(itemStack)) {
            amount += itemStack.amount
        }
    }
    return amount
}
