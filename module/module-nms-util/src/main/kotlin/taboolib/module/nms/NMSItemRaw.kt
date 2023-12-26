@file:Isolated
package taboolib.module.nms

import org.bukkit.inventory.meta.ItemMeta
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.Isolated
import taboolib.common.UnsupportedVersionException
import taboolib.module.chat.Source

/**
 * 将 [Source] 写入物品的显示名称
 */
fun ItemMeta.setDisplayName(source: Source): ItemMeta {
    if (MinecraftVersion.isLower(MinecraftVersion.V1_17)) {
        throw UnsupportedVersionException()
    }
    setProperty("displayName", source.toRawMessage())
    return this
}

/**
 * 将 [Source] 写入物品的描述
 */
fun ItemMeta.setLore(source: List<Source>): ItemMeta {
    if (MinecraftVersion.isLower(MinecraftVersion.V1_17)) {
        throw UnsupportedVersionException()
    }
    setProperty("lore", source.map { it.toRawMessage() })
    return this
}