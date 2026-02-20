package taboolib.module.nms.component.internal

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.resources.MinecraftKey
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion.versionId
import taboolib.module.nms.component.ComposedType
import taboolib.module.nms.remap.DynamicOpcode
import taboolib.module.nms.remap.dynamic

/**
 * DamageResistantType
 * 
 * @author TheFloodDragon
 * @since 2026/2/20 08:49
 */
@Suppress("unused")
class DamageResistantType : ComposedType<String>() {

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

    override fun get(item: Any): String? {
        if (versionId < 12102) throw UnsupportedVersionException()
        // public record DamageResistant(TagKey<DamageType> types)
        val component = (item as ItemStack).get(DAMAGE_RESISTANT) ?: return null
        val tagKey = dynamic(
            DynamicOpcode.INVOKEVIRTUAL,
            "net.minecraft.world.item.component.DamageResistant#types()net.minecraft.tags.TagKey;",
            component,
        ) as TagKey<*>
        return tagKey.location().toString()
    }

    override fun set(item: Any, value: String) {
        if (versionId < 12102) throw UnsupportedVersionException()
        val tagKey = TagKey.create(Registries.DAMAGE_TYPE, MinecraftKey.tryParse(value) ?: error("Invaild key '$value'!"))
        // public record DamageResistant(TagKey<DamageType> types)
        val dataResistant = dynamic(
            DynamicOpcode.INVOKESPECIAL,
            "net.minecraft.world.item.component.DamageResistant(net.minecraft.tags.TagKey;)V",
            tagKey
        )
        (item as ItemStack).set(DAMAGE_RESISTANT, dataResistant)
    }

    override fun remove(item: Any) {
        if (versionId < 12102) throw UnsupportedVersionException()
        (item as ItemStack).remove(DAMAGE_RESISTANT)
    }

}