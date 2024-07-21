package taboolib.module.nms

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.*
import net.minecraft.world.item.component.CustomData
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

/**
 * [NMSItemTag] 的实现类
 */
class NMSItemTagImpl2 : NMSItemTag() {

    private fun getNMSCopy(itemStack: ItemStack): net.minecraft.world.item.ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }

    private fun getBukkitCopy(itemStack: net.minecraft.world.item.ItemStack): ItemStack {
        return CraftItemStack.asBukkitCopy(itemStack)
    }

    override fun getItemTag(itemStack: ItemStack): ItemTag {
        val nmsItem = getNMSCopy(itemStack)
        val tag = nmsItem.get(DataComponents.CUSTOM_DATA)?.copyTag()
        return if (tag != null) itemTagToBukkitCopy(tag).asCompound() else ItemTag()
    }

    override fun setItemTag(itemStack: ItemStack, itemTag: ItemTag): ItemStack {
        val nmsItem = getNMSCopy(itemStack)
        nmsItem.set(DataComponents.CUSTOM_DATA, CustomData.of(itemTagToNMSCopy(itemTag) as NBTTagCompound))
        return getBukkitCopy(nmsItem)
    }

    override fun itemTagToString(itemTagData: ItemTagData): String {
        return itemTagToNMSCopy(itemTagData).toString()
    }

    override fun itemTagToNMSCopy(itemTagData: ItemTagData): NBTBase {
        return when (itemTagData.type) {
            // 基本类型
            ItemTagType.BYTE -> NBTTagByte.valueOf(itemTagData.asByte())
            ItemTagType.SHORT -> NBTTagShort.valueOf(itemTagData.asShort())
            ItemTagType.INT -> NBTTagInt.valueOf(itemTagData.asInt())
            ItemTagType.LONG -> NBTTagLong.valueOf(itemTagData.asLong())
            ItemTagType.FLOAT -> NBTTagFloat.valueOf(itemTagData.asFloat())
            ItemTagType.DOUBLE -> NBTTagDouble.valueOf(itemTagData.asDouble())
            ItemTagType.STRING -> NBTTagString.valueOf(itemTagData.asString())

            // 数组类型特殊处理
            ItemTagType.BYTE_ARRAY -> NBTTagByteArray(itemTagData.asByteArray().copyOf())
            ItemTagType.INT_ARRAY -> NBTTagIntArray(itemTagData.asIntArray().copyOf())
            ItemTagType.LONG_ARRAY -> NBTTagLongArray(itemTagData.asLongArray().copyOf())

            // 列表类型特殊处理
            ItemTagType.LIST -> {
                NBTTagList().also { nmsList ->
                    val dataList = itemTagData.asList()
                    if (dataList.isNotEmpty()) {
                        dataList.forEach { nmsList.add(itemTagToNMSCopy(it)) }
                    }
                }
            }

            // 复合类型特殊处理
            ItemTagType.COMPOUND -> {
                NBTTagCompound().also { nmsCompound ->
                    itemTagData.asCompound().entries.forEach { (key, value) ->
                        nmsCompound.put(key, itemTagToNMSCopy(value))
                    }
                }
            }

            // 不支持的类型
            else -> error("Unsupported type: ${itemTagData.type}}")
        }
    }

    override fun itemTagToBukkitCopy(nbtTag: Any): ItemTagData {
        return when (nbtTag) {
            // 基本类型
            is NBTTagByte -> ItemTagData(ItemTagType.BYTE, nbtTag.asByte)
            is NBTTagShort -> ItemTagData(ItemTagType.SHORT, nbtTag.asShort)
            is NBTTagInt -> ItemTagData(ItemTagType.INT, nbtTag.asInt)
            is NBTTagLong -> ItemTagData(ItemTagType.LONG, nbtTag.asLong)
            is NBTTagFloat -> ItemTagData(ItemTagType.FLOAT, nbtTag.asFloat)
            is NBTTagDouble -> ItemTagData(ItemTagType.DOUBLE, nbtTag.asDouble)
            is NBTTagString -> ItemTagData(ItemTagType.STRING, nbtTag.asString)

            // 数组类型特殊处理
            is NBTTagByteArray -> ItemTagData(ItemTagType.BYTE_ARRAY, nbtTag.asByteArray.copyOf())
            is NBTTagIntArray -> ItemTagData(ItemTagType.INT_ARRAY, nbtTag.asIntArray.copyOf())
            is NBTTagLongArray -> ItemTagData(ItemTagType.LONG_ARRAY, nbtTag.asLongArray.copyOf())

            // 列表类型特殊处理
            is NBTTagList -> {
                ItemTagList(nbtTag.map { itemTagToBukkitCopy(it) })
            }

            // 复合类型特殊处理
            is NBTTagCompound -> {
                ItemTag(nbtTag.allKeys.associateWith { itemTagToBukkitCopy(nbtTag.get(it)!!) })
            }

            // 不支持的类型
            else -> error("Unsupported type: ${nbtTag::class.java}}")
        }
    }
}