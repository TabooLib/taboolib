@file:Isolated

package taboolib.platform.util

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated

fun Player.giveItem(itemStack: ItemStack) {
    inventory.addItem(itemStack).values.forEach { world.dropItem(location, it) }
}

fun Player.getUsingItem(material: Material): ItemStack? {
    return when {
        itemInHand.type == material -> itemInHand
        inventory.itemInOffHand.type == material -> inventory.itemInOffHand
        else -> null
    }
}