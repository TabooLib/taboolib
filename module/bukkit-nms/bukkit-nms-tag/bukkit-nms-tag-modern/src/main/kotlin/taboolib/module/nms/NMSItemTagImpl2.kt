package taboolib.module.nms

import net.minecraft.advancements.criterion.BlockPredicate
import net.minecraft.core.HolderGetter
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.*
import net.minecraft.resources.Identifier
import net.minecraft.world.item.AdventureModePredicate
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.block.Block
import org.bukkit.craftbukkit.CraftRegistry
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.remap.DynamicOpcode
import taboolib.module.nms.remap.dynamic
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

/**
 * [NMSItemTag] 的实现类，使用 Mojang Mapping
 */
class NMSItemTagImpl2 : NMSItemTag() {

    override fun newItemTag(): ItemTag {
        return ItemTag12005()
    }

    override fun toMinecraftJson(itemStack: ItemStack): String {
        return getNMSCopy(itemStack).toNbt().toString()
    }

    override fun fromMinecraftJson(json: String): ItemStack? {
        // 1.20.5 -> MojangsonParser.parseTag(String)
        // 1.21.5 -> MojangsonParser.parseComponentFully(String)
        val compound = TagParser.parseCompoundFully(json)
        return compound.toItemStack()
    }

    override fun getNMSCopy(itemStack: ItemStack): net.minecraft.world.item.ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }

    override fun getBukkitCopy(itemStack: Any): ItemStack {
        // Paper 26.2
        if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V26_2)) {
            return CraftItemStack.asCraftMirror((itemStack as net.minecraft.world.item.ItemStack).copy())
        }
        return CraftItemStack.asBukkitCopy(itemStack as net.minecraft.world.item.ItemStack)
    }

    override fun getItemTag(itemStack: ItemStack, onlyCustom: Boolean): ItemTag {
        val nmsItem = getNMSCopy(itemStack)
        return if (onlyCustom) {
            val originTag = nmsItem.get(DataComponents.CUSTOM_DATA)
            // java.lang.NoSuchMethodError: 'net.minecraft.nbt.NBTTagCompound net.minecraft.world.item.component.CustomData.copyTag()'
            val tag = if (originTag == null) null else dynamic(DynamicOpcode.INVOKEVIRTUAL, "net.minecraft.world.item.component.CustomData#copyTag()net.minecraft.nbt.CompoundTag;", originTag)
            if (tag != null) itemTagToBukkitCopy(tag, true).asCompound() else ItemTag()
        } else {
            val tag = nmsItem.toNbt()
            if (tag != null) itemTagToBukkitCopy(tag, false).asCompound() else ItemTag12005() // 返回一个特殊的 ItemTag
        }
    }

    override fun setItemTag(itemStack: ItemStack, itemTag: ItemTag, onlyCustom: Boolean): ItemStack {
        return if (onlyCustom) {
            val nmsItem = getNMSCopy(itemStack)
            nmsItem.set(DataComponents.CUSTOM_DATA, customDataOf(itemTagToNMSCopy(itemTag) as CompoundTag))
            getBukkitCopy(nmsItem)
        } else {
            itemTagToNMSCopy(itemTag).toItemStack() ?: itemStack
        }
    }

    private fun customDataOf(nbt: Any): CustomData {
        return dynamic(
            DynamicOpcode.INVOKESTATIC,
            "net.minecraft.world.item.component.CustomData#of(net.minecraft.nbt.CompoundTag;)net.minecraft.world.item.component.CustomData;",
            nbt
        ) as CustomData
    }

    private fun setAdventurePredicate(
        itemStack: ItemStack,
        blocks: List<String>,
        componentType: DataComponentType<AdventureModePredicate>,
    ): ItemStack {
        val nmsItem = getNMSCopy(itemStack)
        val predicates = blocks.mapNotNull { blockName ->
            val key = Identifier.parse(blockName)
            // 懒得导服务端
            @Suppress("unchecked_cast")
            val blockHolder = (dynamic(
                DynamicOpcode.INVOKEVIRTUAL,
                "net.minecraft.core.Registry#get(net.minecraft.resources.Identifier;)java.util.Optional;",
                key
            ) as Optional<Any>).getOrNull()
            blockHolder?.let { holder ->
                val builder = BlockPredicate.Builder.block()
                val block = dynamic(
                    DynamicOpcode.INVOKEVIRTUAL,
                    "net.minecraft.core.Holder\$Reference#value()java.lang.Object;",
                    holder
                ) as Block
                // 懒得管
                @Suppress("unchecked_cast")
                val blockRegistry = dynamic(DynamicOpcode.GETSTATIC, "net.minecraft.core.registries.BuiltInRegistries#BLOCK:net.minecraft.core.DefaultedRegistry;") as HolderGetter<Block>
                builder.of(blockRegistry, block)
                builder.build()
            }
        }
        val predicate = dynamic(DynamicOpcode.INVOKESPECIAL, "net.minecraft.world.item.AdventureModePredicate(java.util.List;)V", predicates) as AdventureModePredicate
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

    override fun itemTagToNMSCopy(itemTagData: ItemTagData): Tag {
        return when (itemTagData.type) {
            // 基本类型
            ItemTagType.BYTE -> ByteTag.valueOf(itemTagData.asByte())
            ItemTagType.SHORT -> ShortTag.valueOf(itemTagData.asShort())
            ItemTagType.INT -> IntTag.valueOf(itemTagData.asInt())
            ItemTagType.LONG -> LongTag.valueOf(itemTagData.asLong())
            ItemTagType.FLOAT -> FloatTag.valueOf(itemTagData.asFloat())
            ItemTagType.DOUBLE -> DoubleTag.valueOf(itemTagData.asDouble())
            ItemTagType.STRING -> StringTag.valueOf(itemTagData.asString())

            // 数组类型特殊处理
            ItemTagType.BYTE_ARRAY -> ByteArrayTag(itemTagData.asByteArray().copyOf())
            ItemTagType.INT_ARRAY -> IntArrayTag(itemTagData.asIntArray().copyOf())
            ItemTagType.LONG_ARRAY -> LongArrayTag(itemTagData.asLongArray().copyOf())

            // 列表类型特殊处理
            ItemTagType.LIST -> {
                ListTag().also { nmsList ->
                    val dataList = itemTagData.asList()
                    if (dataList.isNotEmpty()) {
                        dataList.forEach { nmsList.add(itemTagToNMSCopy(it)) }
                    }
                }
            }

            // 复合类型特殊处理
            ItemTagType.COMPOUND -> {
                CompoundTag().also { nmsCompound ->
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
            is ByteTag -> ItemTagData(
                ItemTagType.BYTE,
                nbtTag.value()
            )

            is ShortTag -> ItemTagData(
                ItemTagType.SHORT,
                nbtTag.value()
            )

            is IntTag -> ItemTagData(
                ItemTagType.INT,
                nbtTag.value()
            )

            is LongTag -> ItemTagData(
                ItemTagType.LONG,
                nbtTag.value()
            )

            is FloatTag -> ItemTagData(
                ItemTagType.FLOAT,
                nbtTag.value()
            )

            is DoubleTag -> ItemTagData(
                ItemTagType.DOUBLE,
                nbtTag.value()
            )

            is StringTag -> ItemTagData(
                ItemTagType.STRING,
                nbtTag.value()
            )

            // 数组类型特殊处理
            is ByteArrayTag -> ItemTagData(ItemTagType.BYTE_ARRAY, nbtTag.asByteArray.copyOf())
            is IntArrayTag -> ItemTagData(ItemTagType.INT_ARRAY, nbtTag.asIntArray.copyOf())
            is LongArrayTag -> ItemTagData(ItemTagType.LONG_ARRAY, nbtTag.asLongArray.copyOf())

            // 列表类型特殊处理
            is ListTag -> {
                ItemTagList(nbtTag.map { itemTagToBukkitCopy(it) })
            }

            // 复合类型特殊处理
            is CompoundTag -> {
                nbtTag.keySet().associateWith { itemTagToBukkitCopy(nbtTag.get(it)!!) }.let {
                    if (onlyCustom) ItemTag(it) else ItemTag12005(it)
                }
            }

            // 不支持的类型
            else -> error("Unsupported type: ${nbtTag::class.java}}")
        }
    }

    val nmsItemStackCodeC = dynamic(
        DynamicOpcode.GETSTATIC,
        "net.minecraft.world.item.ItemStack#CODEC:com.mojang.serialization.Codec;"
    )
    val nbtOpsInstance = dynamic(
        DynamicOpcode.GETSTATIC,
        "net.minecraft.nbt.NbtOps#INSTANCE:net.minecraft.nbt.NbtOps;"
    )

    // 12106
    private fun net.minecraft.world.item.ItemStack.toNbt(): Tag? {
        // NMSItemStack.CODEC.encodeStart(CraftRegistry.getMinecraftRegistry().createSerializationContext(DynamicOpsNBT.INSTANCE), this).result().getOrNull()
        // java.lang.IncompatibleClassChangeError: Found interface com.mojang.serialization.DataResult, but class was expected

        // java 17
        val serializationContext = dynamic(
            DynamicOpcode.INVOKEVIRTUAL,
            "net.minecraft.core.HolderLookup\$Provider#createSerializationContext(com.mojang.serialization.DynamicOps;)net.minecraft.resources.RegistryOps;",
            CraftRegistry.getMinecraftRegistry(),
            nbtOpsInstance
        )
        val dataResult = dynamic(
            DynamicOpcode.INVOKEVIRTUAL,
            "com.mojang.serialization.Encoder#encodeStart(com.mojang.serialization.DynamicOps;java.lang.Object;)com.mojang.serialization.DataResult;",
            nmsItemStackCodeC,
            serializationContext,
            this
        )
        @Suppress("unchecked_cast")
        val optional = dynamic(DynamicOpcode.INVOKEVIRTUAL, "com.mojang.serialization.DataResult#result()java.util.Optional;", dataResult) as Optional<Tag>
        return optional.getOrNull()
    }

    // 12106
    private fun Tag.toItemStack(): ItemStack? {
        // NMSItemStack.CODEC.parse(CraftRegistry.getMinecraftRegistry().createSerializationContext(DynamicOpsNBT.INSTANCE), this)
        // java.lang.IncompatibleClassChangeError: Found interface com.mojang.serialization.DataResult, but class was expected

        // java 17
        val serializationContext = dynamic(
            DynamicOpcode.INVOKEVIRTUAL,
            "net.minecraft.core.HolderLookup\$Provider#createSerializationContext(com.mojang.serialization.DynamicOps;)net.minecraft.resources.RegistryOps;",
            CraftRegistry.getMinecraftRegistry(),
            nbtOpsInstance
        )
        val dataResult = dynamic(
            DynamicOpcode.INVOKEVIRTUAL,
            "com.mojang.serialization.Decoder#parse(com.mojang.serialization.DynamicOps;java.lang.Object;)com.mojang.serialization.DataResult;",
            nmsItemStackCodeC,
            serializationContext,
            this
        )
        @Suppress("unchecked_cast")
        val optional = dynamic(DynamicOpcode.INVOKEVIRTUAL, "com.mojang.serialization.DataResult#result()java.util.Optional;", dataResult) as Optional<net.minecraft.world.item.ItemStack>
        val nmsItem = optional.getOrNull()
        return if (nmsItem != null) getBukkitCopy(nmsItem) else null
    }
}