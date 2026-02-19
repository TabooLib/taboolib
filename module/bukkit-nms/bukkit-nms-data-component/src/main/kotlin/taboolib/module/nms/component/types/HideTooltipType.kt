package taboolib.module.nms.component.types

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType

/**
 * HideTooltipType - 完全隐藏物品提示框（1.20.5+）
 *
 * 存在该组件时，鼠标悬停不会显示任何提示信息
 *
 * @author TheFloodDragon
 * @since 2026/2/19
 */
@Suppress("unused")
class HideTooltipType : ComposedType<Boolean>() {

    override fun get(item: Any): Boolean? {
        if (MinecraftVersion.versionId >= 12005) {
            return if ((item as ItemStack).has(DataComponents.HIDE_TOOLTIP)) true else null
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: Boolean) {
        item as ItemStack
        if (MinecraftVersion.versionId >= 12005) {
            if (value) {
                item.set(DataComponents.HIDE_TOOLTIP, net.minecraft.util.Unit.INSTANCE)
            } else {
                item.remove(DataComponents.HIDE_TOOLTIP)
            }
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.HIDE_TOOLTIP)
        } else throw UnsupportedVersionException()
    }

}
