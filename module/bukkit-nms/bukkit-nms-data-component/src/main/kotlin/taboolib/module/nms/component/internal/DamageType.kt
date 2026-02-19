package taboolib.module.nms.component.internal

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType

/**
 * DamageType - 物品耐久损耗值
 *
 * @author TheFloodDragon
 * @since 2026/2/19
 */
@Suppress("unused")
class DamageType : ComposedType<Int>() {

    override fun get(item: Any): Int? {
        if (MinecraftVersion.versionId >= 12005) {
            return (item as ItemStack).get(DataComponents.DAMAGE)
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: Int) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).set(DataComponents.DAMAGE, value)
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.DAMAGE)
        } else throw UnsupportedVersionException()
    }

}
