package taboolib.module.nms.component.types

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType

/**
 * MaxDamageType - 物品最大耐久值（1.20.5+）
 *
 * @author TheFloodDragon
 * @since 2026/2/19
 */
@Suppress("unused")
class MaxDamageType : ComposedType<Int>() {

    override fun get(item: Any): Int? {
        if (MinecraftVersion.versionId >= 12005) {
            return (item as ItemStack).get(DataComponents.MAX_DAMAGE)
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: Int) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).set(DataComponents.MAX_DAMAGE, value)
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.MAX_DAMAGE)
        } else throw UnsupportedVersionException()
    }

}
