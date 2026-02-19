package taboolib.module.nms.component.internal

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.util.Unit
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.Unbreakable
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType

/**
 * UnbreakableType
 *
 * @author TheFloodDragon
 * @since 2026/2/19 18:07
 */
@Suppress("unused")
class UnbreakableType : ComposedType<Boolean>() {

    override fun get(item: Any): Boolean? {
        if (MinecraftVersion.versionId >= 12005) {
            // 有 unbreakable 组件则返回 true，无则返回 null（表示组件不存在）
            // null 是一个必要的状态, 因为物品是否含有该组件需要通过此来判断
            return if ((item as ItemStack).has(DataComponents.UNBREAKABLE)) true else null
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: Boolean) {
        item as ItemStack
        if (MinecraftVersion.versionId >= 12005) {
            if (value) {
                if (MinecraftVersion.versionId >= 12105) {
                    @Suppress("unchecked_cast")
                    item.set(DataComponents.UNBREAKABLE as DataComponentType<Unit>, Unit.INSTANCE)
                } else {
                    // 添加 Unbreakable 组件（showInTooltip = true 默认展示提示）
                    item.set(DataComponents.UNBREAKABLE, Unbreakable(true))
                }
            } else {
                item.remove(DataComponents.UNBREAKABLE)
            }
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.UNBREAKABLE)
        } else throw UnsupportedVersionException()
    }

}