package taboolib.module.nms

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.*
import net.minecraft.world.item.component.CustomData
import org.bukkit.craftbukkit.v1_21_R4.CraftRegistry
import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

/**
 * [NMSItemTag] 的实现类
 */
class NMSItemTag12105 : NMSItemTag() {

    override fun newItemTag(): ItemTag {
        return ItemTag12105()
    }

    override fun toMinecraftJson(itemStack: ItemStack): String {
        return getNMSCopy(itemStack).save(CraftRegistry.getMinecraftRegistry()).toString()
    }

    override fun fromMinecraftJson(json: String): ItemStack? {
        // 1.20.5 -> MojangsonParser.parseTag(String)
        // 1.21.5 -> MojangsonParser.parseComponentFully(String)
        val nmsItem = net.minecraft.world.item.ItemStack.parse(CraftRegistry.getMinecraftRegistry(), MojangsonParser.parseCompoundFully(json)).getOrNull()
        return if (nmsItem != null) getBukkitCopy(nmsItem) else null
    }

    override fun getNMSCopy(itemStack: ItemStack): net.minecraft.world.item.ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }

    override fun getBukkitCopy(itemStack: Any): ItemStack {
        return CraftItemStack.asBukkitCopy(itemStack as net.minecraft.world.item.ItemStack)
    }

    override fun getItemTag(itemStack: ItemStack, onlyCustom: Boolean): ItemTag {
        val nmsItem = getNMSCopy(itemStack)
        return if (onlyCustom) {
            val tag = nmsItem.get(DataComponents.CUSTOM_DATA)?.copyTag()
            if (tag != null) itemTagToBukkitCopy(tag).asCompound() else ItemTag()
        } else {
            val tag = nmsItem.save(CraftRegistry.getMinecraftRegistry())
            if (tag != null) itemTagToBukkitCopy(tag, true).asCompound() else ItemTag12105() // 返回一个特殊的 ItemTag
        }
    }

    override fun setItemTag(itemStack: ItemStack, itemTag: ItemTag, onlyCustom: Boolean): ItemStack {
        return if (onlyCustom) {
            val nmsItem = getNMSCopy(itemStack)
            nmsItem.set(DataComponents.CUSTOM_DATA, CustomData.of(itemTagToNMSCopy(itemTag) as NBTTagCompound))
            getBukkitCopy(nmsItem)
        } else {
            val nmsItem = net.minecraft.world.item.ItemStack.parse(CraftRegistry.getMinecraftRegistry(), itemTagToNMSCopy(itemTag))
            if (nmsItem.isPresent) getBukkitCopy(nmsItem.get()) else itemStack
        }
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
        return itemTagToBukkitCopy(nbtTag, false)
    }

    private fun itemTagToBukkitCopy(nbtTag: Any, onlyCustom: Boolean): ItemTagData {
        return when (nbtTag) {
            // 基本类型
            is NBTTagByte -> ItemTagData(ItemTagType.BYTE, nbtTag.value)
            is NBTTagShort -> ItemTagData(ItemTagType.SHORT, nbtTag.value)
            is NBTTagInt -> ItemTagData(ItemTagType.INT, nbtTag.value)
            is NBTTagLong -> ItemTagData(ItemTagType.LONG, nbtTag.value)
            is NBTTagFloat -> ItemTagData(ItemTagType.FLOAT, nbtTag.value)
            is NBTTagDouble -> ItemTagData(ItemTagType.DOUBLE, nbtTag.value)
            is NBTTagString -> ItemTagData(ItemTagType.STRING, nbtTag.value)

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
                // 1.20.5 -> nbtTag.allKeys.xxx
                // 1.21.5 -> nbtTag.keySet()
                nbtTag.keySet().associateWith { itemTagToBukkitCopy(nbtTag.get(it)!!) }.let {
                    if (onlyCustom) ItemTag(it) else ItemTag12105(it)
                }
            }

            // 不支持的类型
            else -> error("Unsupported type: ${nbtTag::class.java}}")
        }
    }
}

class ItemTag12105 : ItemTag {

    constructor() : super()
    constructor(map: Map<String, ItemTagData>) : super(map)

    /**
     * 在 1.20.5 上将完整的 [ItemTag]（包含类型、数量等之前没有的信息）写入物品
     */
    override fun saveTo(item: ItemStack, onlyCustom: Boolean): ItemStack {
        val newItem = item.setItemTag(this, onlyCustom)
        item.type = newItem.type
        item.amount = newItem.amount
        item.durability = newItem.durability
        item.itemMeta = newItem.itemMeta
        return item
    }
}