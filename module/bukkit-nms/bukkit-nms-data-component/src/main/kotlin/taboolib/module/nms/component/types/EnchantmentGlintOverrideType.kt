package taboolib.module.nms.component.types

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType

/**
 * EnchantmentGlintOverrideType - 附魔光效强制覆盖（1.20.5+）
 *
 * true  = 强制显示附魔光效（无附魔时也显示）
 * false = 强制隐藏附魔光效（有附魔时也不显示）
 * null  = 使用默认行为（有附魔则显示）
 *
 * @author TheFloodDragon
 * @since 2026/2/19
 */
@Suppress("unused")
class EnchantmentGlintOverrideType : ComposedType<Boolean>() {

    override fun get(item: Any): Boolean? {
        if (MinecraftVersion.versionId >= 12005) {
            return (item as ItemStack).get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: Boolean) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, value)
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)
        } else throw UnsupportedVersionException()
    }

}
