package taboolib.module.nms.component.types

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import org.bukkit.craftbukkit.v1_20_R4.util.CraftChatMessage
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.nms.component.ComposedType

/**
 * LoreType
 * 
 * @author TheFloodDragon
 * @since 2026/2/19 16:32
 */
@Suppress("unused")
class LoreType : ComposedType<List<ComponentText>>() {

    override val dataComponentType: Any = DataComponents.LORE

    override fun getRaw(item: Any): Any? {
        return (item as ItemStack).get(DataComponents.LORE)
    }

    override fun setRaw(item: Any, value: Any) {
        (item as ItemStack).set(DataComponents.LORE, value as ItemLore)
    }

    override fun get(item: Any): List<ComponentText>? {
        val itemLore = getRaw(item) as? ItemLore? ?: return null
        return itemLore.lines.map {
            Components.parseRaw(CraftChatMessage.toJSON(it))
        }
    }

    override fun set(item: Any, value: List<ComponentText>) {
        val itemLore = ItemLore(value.map {
            CraftChatMessage.fromJSON(it.toRawMessage())
        })
        setRaw(item, itemLore)
    }

    override fun remove(item: Any) {
        (item as ItemStack).remove(DataComponents.LORE)
    }

}