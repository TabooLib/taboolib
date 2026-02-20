package taboolib.module.nms.component.internal

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomModelData
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType
import taboolib.module.nms.remap.DynamicOpcode
import taboolib.module.nms.remap.dynamic

/**
 * CustomModelDataType - 物品自定义模型数据
 *
 * @author TheFloodDragon
 * @since 2026/2/19
 */
@Suppress("unused")
class CustomModelDataType : ComposedType<Int>() {

    override fun get(item: Any): Int? {
        // FIXME 精确版本
        if (MinecraftVersion.versionId >= 12104) {
            // CraftMetaItem#getCustomModelData:
            // return ((Float)this.customModelData.getFloats().get(0)).intValue();
            val customModelData = (item as ItemStack).get(DataComponents.CUSTOM_MODEL_DATA)
            @Suppress("unchecked_cast")
            val floats = dynamic(
                DynamicOpcode.INVOKEVIRTUAL,
                "net.minecraft.v12105.world.item.component.CustomModelData#floats()java.util.List;",
                customModelData
            ) as List<Float>
            return floats.first().toInt() // Bukkit 这么写的
        } else if (MinecraftVersion.versionId >= 12005) {
            return (item as ItemStack).get(DataComponents.CUSTOM_MODEL_DATA)?.value
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: Int) {
        // FIXME 精确版本
        if (MinecraftVersion.versionId >= 12104) {
            // CraftMetaItem#setCustomModelData:
            // this.customModelData = data == null ? null : new CraftCustomModelDataComponent(new CustomModelData(List.of(data.floatValue()), List.of(), List.of(), List.of()));
            // 依旧是 Bukkit 这么写的
            val customModelData = dynamic(
                DynamicOpcode.INVOKESPECIAL,
                "net.minecraft.world.item.component.CustomModelData(java.util.List;java.util.List;java.util.List;java.util.List;)V",
                listOf(value.toFloat()), listOf<Boolean>(), listOf<String>(), listOf<Int>()
            ) as CustomModelData
            (item as ItemStack).set(DataComponents.CUSTOM_MODEL_DATA, customModelData)
        } else if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(value))
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.CUSTOM_MODEL_DATA)
        } else throw UnsupportedVersionException()
    }

}
