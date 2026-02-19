package taboolib.module.nms.component.types

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import org.bukkit.craftbukkit.v1_20_R4.util.CraftChatMessage
import taboolib.common.UnsupportedVersionException
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType

/**
 * ItemNameType - 物品基础名称
 *
 * 与 CUSTOM_NAME 不同：ITEM_NAME 不显示为斜体，且在有附魔时也会显示
 *
 * @author TheFloodDragon
 * @since 2026/2/19
 */
@Suppress("unused")
class ItemNameType : ComposedType<ComponentText>() {

    override fun get(item: Any): ComponentText? {
        if (MinecraftVersion.versionId >= 12005) {
            val component = (item as ItemStack).get(DataComponents.ITEM_NAME) ?: return null
            return Components.parseRaw(CraftChatMessage.toJSON(component))
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: ComponentText) {
        if (MinecraftVersion.versionId >= 12005) {
            val component = CraftChatMessage.fromJSON(value.toRawMessage())
            (item as ItemStack).set(DataComponents.ITEM_NAME, component)
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.ITEM_NAME)
        } else throw UnsupportedVersionException()
    }

}
