package taboolib.module.nms

import net.minecraft.advancements.critereon.CriterionConditionBlock
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.*
import net.minecraft.resources.MinecraftKey
import net.minecraft.world.item.AdventureModePredicate
import net.minecraft.world.item.component.CustomData
import org.bukkit.craftbukkit.v1_21_R3.CraftRegistry
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.remap.DynamicOpcode
import taboolib.module.nms.remap.dynamic
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

/**
 * [NMSItemTag] 的实现类
 */
class NMSItemTagImpl : NMSItemTag() {

    override fun newItemTag(): ItemTag {
        return ItemTag12005()
    }

    override fun toMinecraftJson(itemStack: ItemStack): String {
        if (versionId >= 12106) {
            return getNMSCopy(itemStack).toNbt().toString()
        }
        return getNMSCopy(itemStack).save(CraftRegistry.getMinecraftRegistry()).toString()
    }

    override fun fromMinecraftJson(json: String): ItemStack? {
        // 1.20.5 -> MojangsonParser.parseTag(String)
        // 1.21.5 -> MojangsonParser.parseComponentFully(String)
        val compound = if (versionId >= 12105) {
            dynamic(
                DynamicOpcode.INVOKESTATIC,
                "net.minecraft.nbt.MojangsonParser#parseComponentFully(java.lang.String;)net.minecraft.nbt.NBTTagCompound;",
                json
            )
        } else {
            dynamic(
                DynamicOpcode.INVOKESTATIC,
                "net.minecraft.nbt.MojangsonParser#parseTag(java.lang.String;)net.minecraft.nbt.NBTTagCompound;",
                json
            )
        } as NBTTagCompound

        if (versionId >= 12106) {
            return compound.toItemStack()
        }

        val nmsItem = net.minecraft.world.item.ItemStack.parse(
            CraftRegistry.getMinecraftRegistry(),
            compound
        ).getOrNull()
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
            if (tag != null) itemTagToBukkitCopy(tag, true).asCompound() else ItemTag()
        } else {
            val tag = if (versionId >= 12106) nmsItem.toNbt() else nmsItem.save(CraftRegistry.getMinecraftRegistry())
            if (tag != null) itemTagToBukkitCopy(tag, false).asCompound() else ItemTag12005() // 返回一个特殊的 ItemTag
        }
    }

    override fun setItemTag(itemStack: ItemStack, itemTag: ItemTag, onlyCustom: Boolean): ItemStack {
        return if (onlyCustom) {
            val nmsItem = getNMSCopy(itemStack)
            nmsItem.set(DataComponents.CUSTOM_DATA, CustomData.of(itemTagToNMSCopy(itemTag) as NBTTagCompound))
            getBukkitCopy(nmsItem)
        } else {
            if (versionId >= 12106) {
                itemTagToNMSCopy(itemTag).toItemStack() ?: itemStack
            } else {
                val nmsItem = net.minecraft.world.item.ItemStack.parse(
                    CraftRegistry.getMinecraftRegistry(),
                    itemTagToNMSCopy(itemTag)
                )
                if (nmsItem.isPresent) getBukkitCopy(nmsItem.get()) else itemStack
            }
        }
    }

    private fun setAdventurePredicate(
        itemStack: ItemStack,
        blocks: List<String>,
        componentType: DataComponentType<AdventureModePredicate>,
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
        val predicate = if (versionId >= 12105) {
            dynamic(DynamicOpcode.INVOKESPECIAL, "net.minecraft.world.item.AdventureModePredicate(java.util.List;)V", predicates)
        } else {
            dynamic(DynamicOpcode.INVOKESPECIAL, "net.minecraft.world.item.AdventureModePredicate(java.util.List;Z)V", predicates, true)
        } as AdventureModePredicate
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
            is NBTTagByte -> ItemTagData(
                ItemTagType.BYTE,
                (if (versionId >= 12105) dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagByte#value()B", nbtTag)
                else dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagByte#getAsByte()B", nbtTag)) as Byte
            )

            is NBTTagShort -> ItemTagData(
                ItemTagType.SHORT,
                (if (versionId >= 12105) dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagShort#value()S", nbtTag)
                else dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagShort#getAsShort()S", nbtTag)) as Short
            )

            is NBTTagInt -> ItemTagData(
                ItemTagType.INT,
                (if (versionId >= 12105) dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagInt#value()I", nbtTag)
                else dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagInt#getAsInt()I", nbtTag)) as Int
            )

            is NBTTagLong -> ItemTagData(
                ItemTagType.LONG,
                (if (versionId >= 12105) dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagLong#value()J", nbtTag)
                else dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagLong#getAsLong()J", nbtTag)) as Long
            )

            is NBTTagFloat -> ItemTagData(
                ItemTagType.FLOAT,
                (if (versionId >= 12105) dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagFloat#value()F", nbtTag)
                else dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagFloat#getAsFloat()F", nbtTag)) as Float
            )

            is NBTTagDouble -> ItemTagData(
                ItemTagType.DOUBLE,
                (if (versionId >= 12105) dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagDouble#value()D", nbtTag)
                else dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagDouble#getAsDouble()D", nbtTag)) as Double
            )

            is NBTTagString -> ItemTagData(
                ItemTagType.STRING,
                (if (versionId >= 12105) dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagString#value()java.lang.String;", nbtTag)
                else dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagString#getAsString()java.lang.String;", nbtTag)) as String,
            )

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
                @Suppress("unchecked_cast")
                val keySet = if (versionId >= 12105) {
                    dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagCompound#keySet()java.util.Set;", nbtTag)
                } else {
                    dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.nbt.NBTTagCompound#getAllKeys()java.util.Set;", nbtTag)
                } as Set<String>

                keySet.associateWith { itemTagToBukkitCopy(nbtTag.get(it)!!) }.let {
                    if (onlyCustom) ItemTag(it) else ItemTag12005(it)
                }
            }

            // 不支持的类型
            else -> error("Unsupported type: ${nbtTag::class.java}}")
        }
    }

    // 12106
    private fun net.minecraft.world.item.ItemStack.toNbt(): NBTBase? {
        // NMSItemStack.CODEC.encodeStart(CraftRegistry.getMinecraftRegistry().createSerializationContext(DynamicOpsNBT.INSTANCE), this).result().getOrNull()
        // java.lang.IncompatibleClassChangeError: Found interface com.mojang.serialization.DataResult, but class was expected
        val dataResult = dynamic(
            DynamicOpcode.INVOKEVIRTUAL,
            "com.mojang.serialization.Encoder#encodeStart(com.mojang.serialization.DynamicOps;java.lang.Object;)com.mojang.serialization.DataResult;",
            net.minecraft.world.item.ItemStack.CODEC,
            CraftRegistry.getMinecraftRegistry().createSerializationContext(DynamicOpsNBT.INSTANCE),
            this
        )
        @Suppress("unchecked_cast")
        val optional = dynamic(DynamicOpcode.INVOKEVIRTUAL, "com.mojang.serialization.DataResult#result()java.util.Optional;", dataResult) as Optional<NBTBase>
        return optional.getOrNull()
    }

    // 12106
    private fun NBTBase.toItemStack(): ItemStack? {
        // NMSItemStack.CODEC.parse(CraftRegistry.getMinecraftRegistry().createSerializationContext(DynamicOpsNBT.INSTANCE), this)
        // java.lang.IncompatibleClassChangeError: Found interface com.mojang.serialization.DataResult, but class was expected
        val dataResult = dynamic(
            DynamicOpcode.INVOKEVIRTUAL,
            "com.mojang.serialization.Decoder#parse(com.mojang.serialization.DynamicOps;java.lang.Object;)com.mojang.serialization.DataResult;",
            net.minecraft.world.item.ItemStack.CODEC,
            CraftRegistry.getMinecraftRegistry().createSerializationContext(DynamicOpsNBT.INSTANCE),
            this
        )
        @Suppress("unchecked_cast")
        val optional = dynamic(DynamicOpcode.INVOKEVIRTUAL, "com.mojang.serialization.DataResult#result()java.util.Optional;", dataResult) as Optional<net.minecraft.world.item.ItemStack>
        val nmsItem = optional.getOrNull()
        return if (nmsItem != null) getBukkitCopy(nmsItem) else null
    }
}

class ItemTag12005 : ItemTag {

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