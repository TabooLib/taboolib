package taboolib.library.xseries

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.colored

/**
 * 将 ItemStack 序列化并保存到配置节点中。
 *
 * @param node 要保存的配置节点名称
 * @param itemStack 要序列化的 ItemStack 对象
 */
fun ConfigurationSection.setItemStack(node: String, itemStack: ItemStack) {
    XItemStack.serialize(itemStack, createSection(node))
}

/**
 * 从配置节点中反序列化 ItemStack 对象。
 *
 * @param node 要读取的配置节点名称
 * @return 反序列化后的 ItemStack 对象，如果节点不存在则返回 null
 */
fun ConfigurationSection.getItemStack(node: String): ItemStack? {
    val section = getConfigurationSection(node) ?: return null
    return XItemStack.deserialize(section) { it.colored() }
}

/**
 * 从配置节点中反序列化 ItemStack 对象，并应用自定义转换函数。
 *
 * @param node 要读取的配置节点名称
 * @param transfer 用于转换字符串的自定义函数
 * @return 反序列化后的 ItemStack 对象，如果节点不存在则返回 null
 */
fun ConfigurationSection.getItemStack(node: String, transfer: (String) -> String): ItemStack? {
    val section = getConfigurationSection(node) ?: return null
    return XItemStack.deserialize(section, transfer)
}

/**
 * 将字符串解析为 Material 对象。
 *
 * @return 解析后的 Material 对象，如果无法匹配则返回 STONE
 */
fun String.parseToMaterial(): Material {
    return XMaterial.matchXMaterial(this).orElse(XMaterial.STONE).parseMaterial()!!
}

/**
 * 将字符串解析为 XMaterial 对象。
 *
 * @return 解析后的 XMaterial 对象，如果无法匹配则返回 STONE
 */
fun String.parseToXMaterial(): XMaterial {
    return XMaterial.matchXMaterial(this).orElse(XMaterial.STONE)
}

/**
 * 将字符串解析为 ItemStack 对象。
 *
 * @return 解析后的 ItemStack 对象，如果无法匹配则返回 STONE 的 ItemStack
 */
fun String.parseToItemStack(): ItemStack {
    return XMaterial.matchXMaterial(this).orElse(XMaterial.STONE).parseItem()!!
}