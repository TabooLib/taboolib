package taboolib.module.nms.component.internal

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType

/**
 * FireResistantType - 物品防火属性（1.20.5+）
 *
 * 存在该组件时，物品在熔岩/火焰中不会被销毁（如下界合金物品）
 *
 * @author TheFloodDragon
 * @since 2026/2/19
 */
@Suppress("unused")
class FireResistantType : ComposedType<Boolean>() {

    override fun get(item: Any): Boolean? {
        if (MinecraftVersion.versionId >= 12005) {
            return if ((item as ItemStack).has(DataComponents.FIRE_RESISTANT)) true else null
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: Boolean) {
        item as ItemStack
        if (MinecraftVersion.versionId >= 12005) {
            if (value) {
                item.set(DataComponents.FIRE_RESISTANT, net.minecraft.util.Unit.INSTANCE)
            } else {
                item.remove(DataComponents.FIRE_RESISTANT)
            }
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.FIRE_RESISTANT)
        } else throw UnsupportedVersionException()
    }

}
