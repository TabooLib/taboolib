package taboolib.platform.util

import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.adaptPlayer

/**
 * 获取玩家背包中的空槽位数量。
 *
 * @param hasEquipment 是否计算装备栏，默认为 false。
 * @param isItemAmount 是否返回可存放的物品总数而非空槽位数，默认为 false。
 * @return 如果 isItemAmount 为 false，返回空槽位数；否则返回可存放的物品总数（每个槽位可存放 64 个物品）。
 */
fun HumanEntity.getEmptySlot(hasEquipment: Boolean = false, isItemAmount: Boolean = false): Int {
    var air = 0
    for (itemStack in inventory.contents) {
        if (itemStack == null || itemStack.type == Material.AIR) {
            air++
        }
    }
    if (hasEquipment) {
        if (inventory.itemInOffHand.type == Material.AIR) air--
        if (inventory.helmet == null) air--
        if (inventory.chestplate == null) air--
        if (inventory.leggings == null) air--
        if (inventory.boots == null) air--
    }
    return if (isItemAmount) air * 64 else air
}

/**
 * 给予玩家多个物品。
 *
 * @param itemStack 要给予的物品列表。
 */
fun HumanEntity.giveItem(itemStack: List<ItemStack>) {
    itemStack.forEach { giveItem(it) }
}

/**
 * 给予玩家物品。
 *
 * @param itemStack 要给予的物品。
 * @param repeat 重复给予的次数，默认为 1。
 */
fun HumanEntity.giveItem(itemStack: ItemStack?, repeat: Int = 1) {
    if (itemStack.isNotAir()) {
        // CraftInventory.addItem 的执行过程中, 实质上有可能修改 ItemStack 的 amount, 如果不注意这一点, 则会吞物品而不自知
        val preAmount = itemStack.amount
        repeat(repeat) {
            inventory.addItem(itemStack).values.forEach { world.dropItem(location, it) }
            itemStack.amount = preAmount
        }
    }
}

/**
 * 获取玩家正在使用的指定材质的物品。
 *
 * @param material 要查找的物品材质。
 * @return 如果玩家主手或副手持有指定材质的物品，则返回该物品；否则返回 null。
 */
fun HumanEntity.getUsingItem(material: Material): ItemStack? {
    return when {
        inventory.itemInMainHand.type == material -> inventory.itemInMainHand
        inventory.itemInOffHand.type == material -> inventory.itemInOffHand
        else -> null
    }
}

/**
 * 向玩家发送动作栏消息。
 *
 * @param message 要发送的消息。
 */
fun HumanEntity.sendActionBar(message: String) {
    adaptPlayer(this).sendActionBar(message)
}

/**
 * 向玩家发送动作栏消息（别名）。
 *
 * @param message 要发送的消息。
 */
fun HumanEntity.actionBar(message: String) {
    adaptPlayer(this).sendActionBar(message)
}

/**
 * 向玩家发送标题和副标题，使用默认的淡入淡出时间。
 *
 * @param title 主标题，可以为 null。
 * @param subTitle 副标题，可以为 null。
 */
fun HumanEntity.title(title: String?, subTitle: String?) {
    adaptPlayer(this).sendTitle(title, subTitle, 10, 60, 10)
}

/**
 * 向玩家发送标题和副标题，可自定义淡入淡出时间。
 *
 * @param title 主标题，可以为 null。
 * @param subTitle 副标题，可以为 null。
 * @param fadeIn 淡入时间（单位：tick）。
 * @param stay 停留时间（单位：tick）。
 * @param fadeOut 淡出时间（单位：tick）。
 */
fun HumanEntity.title(title: String?, subTitle: String?, fadeIn: Int, stay: Int, fadeOut: Int) {
    adaptPlayer(this).sendTitle(title, subTitle, fadeIn, stay, fadeOut)
}

/**
 * 将玩家的饥饿值恢复到最大值（20）。
 */
fun HumanEntity.feed() {
    foodLevel = 20
}

/**
 * 将玩家的饱和度恢复到最大值（20.0）。
 */
fun HumanEntity.saturate() {
    saturation = 20F
}
