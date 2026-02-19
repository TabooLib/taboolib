package taboolib.module.nms.component.internal

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import org.bukkit.craftbukkit.v1_20_R4.util.CraftChatMessage
import taboolib.common.UnsupportedVersionException
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType

/**
 * LoreType
 * 
 * @author TheFloodDragon
 * @since 2026/2/19 16:32
 */
@Suppress("unused")
class LoreType : ComposedType<MutableList<ComponentText>>() {

    override fun get(item: Any): MutableList<ComponentText>? {
        if (MinecraftVersion.versionId >= 12005) {
            val itemLore = (item as ItemStack).get(DataComponents.LORE) ?: return null
            val list = ArrayList<ComponentText>(itemLore.lines.size)
            for (line in itemLore.lines) {
                list.add(Components.parseRaw(CraftChatMessage.toJSON(line)))
            }
            return list
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: MutableList<ComponentText>) {
        if (MinecraftVersion.versionId >= 12005) {
            val itemLore = ItemLore(value.map {
                CraftChatMessage.fromJSON(it.toRawMessage())
            })
            (item as ItemStack).set(DataComponents.LORE, itemLore)
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.LORE)
        } else throw UnsupportedVersionException()
    }

}