@file:Isolated

package taboolib.library.xseries

import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated
import taboolib.library.configuration.ConfigurationSection

fun ConfigurationSection.setItemStack(node: String, itemStack: ItemStack) {
    XItemStack.serialize(itemStack, createSection(node))
}

fun ConfigurationSection.getItemStack(node: String): ItemStack? {
    val section = getConfigurationSection(node) ?: return null
    return XItemStack.deserialize(section)
}