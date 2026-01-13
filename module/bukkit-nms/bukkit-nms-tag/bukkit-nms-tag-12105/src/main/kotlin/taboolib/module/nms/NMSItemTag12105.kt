package taboolib.module.nms

import net.minecraft.advancements.critereon.CriterionConditionBlock
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.*
import net.minecraft.resources.MinecraftKey
import net.minecraft.world.item.AdventureModePredicate
import net.minecraft.world.item.component.CustomData
import org.bukkit.craftbukkit.v1_21_R4.CraftRegistry
import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Modifier
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
        val nmsItem = net.minecraft.world.item.ItemStack.parse(
            CraftRegistry.getMinecraftRegistry(),
            MojangsonParser.parseCompoundFully(json)
        ).getOrNull()
        return if (nmsItem != null) getBukkitCopy(nmsItem) else null
    }

    override fun getNMSCopy(itemStack: ItemStack): net.minecraft.world.item.ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }

    override fun getBukkitCopy(itemStack: Any): ItemStack {
        return CraftItemStack.asBukkitCopy(itemStack as net.minecraft.world.item.ItemStack)
    }

    val any by lazy {
        DataComponents::class.java.declaredFields.filter {
            Modifier.isStatic(it.modifiers) && it.type == DataComponentType::class.java
        }.map {
            it.get(null) as DataComponentType<*>
        }
    }

    override fun getItemTag(itemStack: ItemStack, onlyCustom: Boolean): ItemTag {
        val nmsItem = getNMSCopy(itemStack)
        return if (onlyCustom) {
            val a = any.filter { nmsItem.get(it) != null }.map { it.toString() to itemTagToBukkitCopy(nmsItem.get(it)!!, true) }
            ItemTag(a.associate { it.first to it.second })
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
            val nmsItem = net.minecraft.world.item.ItemStack.parse(
                CraftRegistry.getMinecraftRegistry(),
                itemTagToNMSCopy(itemTag)
            )
            if (nmsItem.isPresent) getBukkitCopy(nmsItem.get()) else itemStack
        }
    }

    private fun setAdventurePredicate(
        itemStack: ItemStack,
        blocks: List<String>,
        componentType: DataComponentType<AdventureModePredicate>
    ): ItemStack {
        val nmsItem = getNMSCopy(itemStack)
        val predicates = blocks.mapNotNull { blockName ->
            val key = MinecraftKey.parse(blockName)
            val blockHolder = BuiltInRegistries.BLOCK.get(key).getOrNull()
            blockHolder?.let { holder ->
                CriterionConditionBlock.a.block()
                    .of(BuiltInRegistries.BLOCK, holder.value())
                    .build()
            }
        }
        val predicate = AdventureModePredicate(predicates)
        nmsItem.set(componentType, predicate)
        return getBukkitCopy(nmsItem)
    }

    override fun setItemCanBreak(itemStack: ItemStack, blocks: List<String>): ItemStack {
        return setAdventurePredicate(itemStack, blocks, DataComponents.CAN_BREAK)
    }

    override fun setItemCanPlaceOn(itemStack: ItemStack, blocks: List<String>): ItemStack {
        return setAdventurePredicate(itemStack, blocks, DataComponents.CAN_PLACE_ON)
    }

    override fun hasItemCanBreak(itemStack: ItemStack): Boolean {
        val nmsItem = getNMSCopy(itemStack)
        return nmsItem.get(DataComponents.CAN_BREAK) != null
    }

    override fun hasItemCanPlaceOn(itemStack: ItemStack): Boolean {
        val nmsItem = getNMSCopy(itemStack)
        return nmsItem.get(DataComponents.CAN_PLACE_ON) != null
    }

    override fun removeItemCanBreak(itemStack: ItemStack): ItemStack {
        val nmsItem = getNMSCopy(itemStack)
        nmsItem.remove(DataComponents.CAN_BREAK)
        return getBukkitCopy(nmsItem)
    }

    override fun removeItemCanPlaceOn(itemStack: ItemStack): ItemStack {
        val nmsItem = getNMSCopy(itemStack)
        nmsItem.remove(DataComponents.CAN_PLACE_ON)
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
        return itemTagToBukkitCopy(nbtTag, false)
    }

    private fun itemTagToBukkitCopy(nbtTag: Any, onlyCustom: Boolean): ItemTagData {
        return when (nbtTag) {
            // 基本类型
            is NBTTagByte -> ItemTagData(ItemTagType.BYTE, nbtTag.value)
            is Byte -> ItemTagData(ItemTagType.BYTE, nbtTag)
            is NBTTagShort -> ItemTagData(ItemTagType.SHORT, nbtTag.value)
            is Short -> ItemTagData(ItemTagType.SHORT, nbtTag)
            is NBTTagInt -> ItemTagData(ItemTagType.INT, nbtTag.value)
            is Int -> ItemTagData(ItemTagType.INT, nbtTag)
            is NBTTagLong -> ItemTagData(ItemTagType.LONG, nbtTag.value)
            is Long -> ItemTagData(ItemTagType.LONG, nbtTag)
            is NBTTagFloat -> ItemTagData(ItemTagType.FLOAT, nbtTag.value)
            is Float -> ItemTagData(ItemTagType.FLOAT, nbtTag)
            is NBTTagDouble -> ItemTagData(ItemTagType.DOUBLE, nbtTag.value)
            is Double -> ItemTagData(ItemTagType.DOUBLE, nbtTag)
            is NBTTagString -> ItemTagData(ItemTagType.STRING, nbtTag.value)

            // 数组类型特殊处理
            is NBTTagByteArray -> ItemTagData(ItemTagType.BYTE_ARRAY, nbtTag.asByteArray.copyOf())
            is ByteArray -> ItemTagData(ItemTagType.BYTE_ARRAY, nbtTag)
            is NBTTagIntArray -> ItemTagData(ItemTagType.INT_ARRAY, nbtTag.asIntArray.copyOf())
            is IntArray -> ItemTagData(ItemTagType.INT_ARRAY, nbtTag)
            is NBTTagLongArray -> ItemTagData(ItemTagType.LONG_ARRAY, nbtTag.asLongArray.copyOf())
            is LongArray -> ItemTagData(ItemTagType.LONG_ARRAY, nbtTag)

            // 列表类型特殊处理
            is NBTTagList -> ItemTagList(nbtTag.map { itemTagToBukkitCopy(it, onlyCustom) })
            is List<*> -> ItemTagList(nbtTag.map { itemTagToBukkitCopy(it!!, onlyCustom) })

            // 复合类型特殊处理
            is NBTTagCompound -> {
                // 1.20.5 -> nbtTag.allKeys.xxx
                // 1.21.5 -> nbtTag.keySet()
                nbtTag.keySet().associateWith { itemTagToBukkitCopy(nbtTag.get(it)!!, onlyCustom) }.let {
                    if (onlyCustom) ItemTag(it) else ItemTag12105(it)
                }
            }

            is Boolean -> ItemTagData(nbtTag)

            // 不支持的类型
            else -> ItemComponent.instance.getTagData(nbtTag) ?: error("Unsupported type: ${nbtTag::class.java}}")
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