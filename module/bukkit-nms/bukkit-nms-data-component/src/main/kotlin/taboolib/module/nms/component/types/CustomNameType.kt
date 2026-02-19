package taboolib.module.nms.component.types

import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.world.item.ItemStack
import org.bukkit.craftbukkit.v1_20_R4.util.CraftChatMessage
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.nms.component.ComposedType

/**
 * CustomNameType
 * 
 * @author TheFloodDragon
 * @since 2026/2/19 17:07
 */
@Suppress("unused")
class CustomNameType : ComposedType<ComponentText>() {

    override val dataComponentType: Any = DataComponents.CUSTOM_NAME

    override fun getRaw(item: Any): Any? {
        return (item as ItemStack).get(DataComponents.CUSTOM_NAME)
    }

    override fun setRaw(item: Any, value: Any) {
        (item as ItemStack).set(DataComponents.CUSTOM_NAME, value as IChatBaseComponent?)
    }

    override fun get(item: Any): ComponentText? {
        val component = getRaw(item) as? IChatBaseComponent? ?: return null
        return Components.parseRaw(CraftChatMessage.toJSON(component))
    }

    override fun set(item: Any, value: ComponentText) {
        val component = CraftChatMessage.fromJSON(value.toRawMessage())
        setRaw(item, component)
    }

    override fun remove(item: Any) {
        (item as ItemStack).remove(DataComponents.CUSTOM_NAME)
    }

}