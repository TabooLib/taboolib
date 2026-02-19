package taboolib.module.nms.component.internal

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType

/**
 * RepairCostType - 物品修复经验倍率
 *
 * @author TheFloodDragon
 * @since 2026/2/19
 */
@Suppress("unused")
class RepairCostType : ComposedType<Int>() {

    override fun get(item: Any): Int? {
        if (MinecraftVersion.versionId >= 12005) {
            return (item as ItemStack).get(DataComponents.REPAIR_COST)
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: Int) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).set(DataComponents.REPAIR_COST, value)
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.REPAIR_COST)
        } else throw UnsupportedVersionException()
    }

}
