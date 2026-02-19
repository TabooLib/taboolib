package taboolib.module.nms.component.types

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType

/**
 * HideAdditionalTooltipType - 隐藏额外提示信息（1.20.5+）
 *
 * 存在该组件时，隐藏物品的附加提示内容（如方块容器内容等）
 * 与 HIDE_TOOLTIP 的区别：保留基础提示（名称、Lore），仅隐藏附加信息
 *
 * @author TheFloodDragon
 * @since 2026/2/19
 */
@Suppress("unused")
class HideAdditionalTooltipType : ComposedType<Boolean>() {

    override fun get(item: Any): Boolean? {
        if (MinecraftVersion.versionId >= 12005) {
            return if ((item as ItemStack).has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) true else null
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: Boolean) {
        item as ItemStack
        if (MinecraftVersion.versionId >= 12005) {
            if (value) {
                item.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, net.minecraft.util.Unit.INSTANCE)
            } else {
                item.remove(DataComponents.HIDE_ADDITIONAL_TOOLTIP)
            }
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.HIDE_ADDITIONAL_TOOLTIP)
        } else throw UnsupportedVersionException()
    }

}
