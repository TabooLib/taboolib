@file:Isolated

package taboolib.library.xseries

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.colored

fun ConfigurationSection.setItemStack(node: String, itemStack: ItemStack) {
    XItemStack.serialize(itemStack, createSection(node))
}

fun ConfigurationSection.getItemStack(node: String): ItemStack? {
    val section = getConfigurationSection(node) ?: return null
    return XItemStack.deserialize(section) { it.colored() }
}

fun ConfigurationSection.getItemStack(node: String, transfer: (String) -> String): ItemStack? {
    val section = getConfigurationSection(node) ?: return null
    return XItemStack.deserialize(section, transfer)
}

fun String.parseToMaterial(): Material {
    return XMaterial.matchXMaterial(this).orElse(XMaterial.STONE).parseMaterial()!!
}

fun String.parseToXMaterial(): XMaterial {
    return XMaterial.matchXMaterial(this).orElse(XMaterial.STONE)
}

fun String.parseToItemStack(): ItemStack {
    return XMaterial.matchXMaterial(this).orElse(XMaterial.STONE).parseItem()!!
}