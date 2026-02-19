package taboolib.module.nms.component.internal

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomModelData
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.component.ComposedType

/**
 * CustomModelDataType - 物品自定义模型数据
 *
 * @author TheFloodDragon
 * @since 2026/2/19
 */
@Suppress("unused")
class CustomModelDataType : ComposedType<Int>() {

    override fun get(item: Any): Int? {
        if (MinecraftVersion.versionId >= 12105) {
            item as net.minecraft.v12105.world.item.ItemStack
            // CraftMetaItem#getCustomModelData:
            // return ((Float)this.customModelData.getFloats().get(0)).intValue();
            return (item.get(net.minecraft.v12105.core.component.DataComponents.CUSTOM_MODEL_DATA)
                ?: return null).floats.first().toInt() // Bukkit 这么写的
        } else if (MinecraftVersion.versionId >= 12005) {
            return (item as ItemStack).get(DataComponents.CUSTOM_MODEL_DATA)?.value
        } else throw UnsupportedVersionException()
    }

    override fun set(item: Any, value: Int) {
        if (MinecraftVersion.versionId >= 12105) {
            item as net.minecraft.v12105.world.item.ItemStack
            // CraftMetaItem#setCustomModelData:
            // this.customModelData = data == null ? null : new CraftCustomModelDataComponent(new CustomModelData(List.of(data.floatValue()), List.of(), List.of(), List.of()));
            // 依旧是 Bukkit 这么写的
            val customModelData = net.minecraft.v12105.world.item.component.CustomModelData(
                listOf(value.toFloat()), listOf(), listOf(), listOf()
            )
            item.set(net.minecraft.v12105.core.component.DataComponents.CUSTOM_MODEL_DATA, customModelData)
        }
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(value))
        } else throw UnsupportedVersionException()
    }

    override fun remove(item: Any) {
        if (MinecraftVersion.versionId >= 12005) {
            (item as ItemStack).remove(DataComponents.CUSTOM_MODEL_DATA)
        } else throw UnsupportedVersionException()
    }

}
