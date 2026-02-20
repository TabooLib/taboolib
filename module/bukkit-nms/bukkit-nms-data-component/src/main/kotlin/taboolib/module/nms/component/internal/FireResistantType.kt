package taboolib.module.nms.component.internal

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.tags.DamageTypeTags
import net.minecraft.world.item.ItemStack
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion.versionId
import taboolib.module.nms.component.ComposedType
import taboolib.module.nms.remap.DynamicOpcode
import taboolib.module.nms.remap.dynamic

/**
 * FireResistantType - 物品防火属性（1.20.5+）
 * 1.21.2 删除了此组件, 改为使用 DamageResistant 组件, 现通过其他方法兼容并维护此组件, 以在 1.20.5~1.21.2 使用
 *
 * 存在该组件时，物品在熔岩/火焰中不会被销毁（如下界合金物品）
 *
 * @author TheFloodDragon
 * @since 2026/2/19
 */
@Suppress("unused")
class FireResistantType : ComposedType<Boolean>() {

    // public static final DataComponentType<DamageResistant> DAMAGE_RESISTANT
    @Suppress("PropertyName")
    val DAMAGE_RESISTANT: DataComponentType<Any>
        get() {
            @Suppress("UNCHECKED_CAST")
            return dynamic(
                DynamicOpcode.GETSTATIC,
                "net.minecraft.core.component.DataComponents#DAMAGE_RESISTANT:net.minecraft.core.component.DataComponentType",
            ) as DataComponentType<Any>
        }

    override fun get(item: Any): Boolean? {
        when {
            versionId >= 12102 -> {
                // public record DamageResistant(TagKey<DamageType> types)
                val component = (item as ItemStack).get(DAMAGE_RESISTANT) ?: return null
                val tagKey = dynamic(
                    DynamicOpcode.INVOKEVIRTUAL,
                    "net.minecraft.world.item.component.DamageResistant#types()net.minecraft.tags.TagKey;",
                    component,
                )
                return DamageTypeTags.IS_FIRE.equals(tagKey)
            }

            versionId >= 12005 -> {
                return if ((item as ItemStack).has(DataComponents.FIRE_RESISTANT)) true else null
            }

            else -> throw UnsupportedVersionException()
        }
    }

    override fun set(item: Any, value: Boolean) {
        when {
            versionId >= 12102 -> {
                item as ItemStack
                if (value) {
                    // public record DamageResistant(TagKey<DamageType> types)
                    val dataResistant = dynamic(
                        DynamicOpcode.INVOKESPECIAL,
                        "net.minecraft.world.item.component.DamageResistant(net.minecraft.tags.TagKey;)V",
                        DamageTypeTags.IS_FIRE
                    )
                    item.set(DAMAGE_RESISTANT, dataResistant)
                } else if (this.get(item) == true) { // 仅在当前具有防火属性时才移除组件, 避免误删其他伤害抗性
                    item.remove(DataComponents.FIRE_RESISTANT)
                }
            }

            versionId >= 12005 -> {
                item as ItemStack
                if (value) {
                    item.set(DataComponents.FIRE_RESISTANT, net.minecraft.util.Unit.INSTANCE)
                } else {
                    item.remove(DataComponents.FIRE_RESISTANT)
                }
            }

            else -> throw UnsupportedVersionException()
        }
    }

    override fun remove(item: Any) {
        when {
            versionId >= 12102 -> (item as ItemStack).remove(DAMAGE_RESISTANT)
            versionId >= 12005 -> (item as ItemStack).remove(DataComponents.FIRE_RESISTANT)
            else -> throw UnsupportedVersionException()
        }
    }

}
