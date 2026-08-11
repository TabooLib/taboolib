package taboolib.platform.util

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

internal data class RemovalPlan<T>(val entry: T, val amount: Int)

internal fun <T> planRemoval(amount: Int, entries: Sequence<T>, amountOf: (T) -> Int): List<RemovalPlan<T>>? {
    if (amount <= 0) {
        return emptyList()
    }
    val plan = ArrayList<RemovalPlan<T>>()
    var remainingAmount = amount
    for (entry in entries) {
        val availableAmount = amountOf(entry)
        if (availableAmount <= 0) {
            continue
        }
        val takenAmount = minOf(availableAmount, remainingAmount)
        plan += RemovalPlan(entry, takenAmount)
        remainingAmount -= takenAmount
        if (remainingAmount == 0) {
            return plan
        }
    }
    return null
}

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
    return if (remove) {
        takeItem(amount) { it.isSimilar(item) }
    } else {
        hasItem(amount) { it.isSimilar(item) }
    }
}

/**
 * 检查背包中符合特定规则的物品是否达到特定该数量
 *
 * @param matcher   规则
 * @param amount    数量
 * @return boolean
 */
fun Inventory.hasItem(amount: Int = 1, matcher: (itemStack: ItemStack) -> Boolean): Boolean {
    if (amount <= 0) {
        return true
    }
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
fun Inventory.takeItem(amount: Int = 1, takeList: MutableList<ItemStack> = mutableListOf(), matcher: (itemStack: ItemStack) -> Boolean): Boolean {
    val matchedItems = contents.asSequence().mapIndexedNotNull { index, itemStack ->
        if (itemStack.isNotAir() && matcher(itemStack)) index to itemStack else null
    }
    val removalPlan = planRemoval(amount, matchedItems) { (_, itemStack) -> itemStack.amount } ?: return false
    val takenItems = ArrayList<ItemStack>(removalPlan.size)
    removalPlan.forEach { (entry, takenAmount) ->
        val (index, itemStack) = entry
        takenItems += itemStack.clone().apply { this.amount = takenAmount }
        if (takenAmount == itemStack.amount) {
            setItem(index, null)
        } else {
            setItem(index, itemStack.clone().apply { this.amount = itemStack.amount - takenAmount })
        }
    }
    takeList += takenItems
    return true
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
