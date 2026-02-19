package taboolib.module.nms.component.types

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
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

    override fun get(item: Any): Boolean {
        if (MinecraftVersion.versionId >= 12005) {
            return (item as ItemStack).has(DataComponents.UNBREAKABLE)
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: Boolean) {
        if (MinecraftVersion.versionId >= 12005) {
            // 高版本有 unbreakable 组件物品就是无法破坏
            (item as ItemStack).remove(DataComponents.UNBREAKABLE)
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.UNBREAKABLE)
        } else throw UnsupportedVersionException()
    }

}