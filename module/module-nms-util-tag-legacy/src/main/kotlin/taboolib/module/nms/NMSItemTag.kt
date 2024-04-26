package taboolib.module.nms

import org.bukkit.inventory.ItemStack
import org.tabooproject.reflex.UnsafeAccess
import java.lang.invoke.MethodHandle

/**
 * [NMSItemTag] 的实现类
 */
@Suppress("SpellCheckingInspection", "UNCHECKED_CAST")
open class NMSItemTagImpl1 : NMSItemTag() {

    val nbtTagCompoundGetter = unreflectGetter<NBTTagCompound12>(if (MinecraftVersion.isUniversal) "x" else "map")
    val nbtTagListGetter = unreflectGetter<NBTTagList12>(if (MinecraftVersion.isUniversal) "c" else "list")
    val nbtTagListTypeSetter = unreflectSetter<NBTTagList12>(if (MinecraftVersion.isUniversal) "w" else "type")
    val nbtTagByteGetter = unreflectGetter<NBTTagByte12>(if (MinecraftVersion.isUniversal) "x" else "data")
    val nbtTagShortGetter = unreflectGetter<NBTTagShort12>(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagIntGetter = unreflectGetter<NBTTagInt12>(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagLongGetter = unreflectGetter<NBTTagLong12>(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagFloatGetter = unreflectGetter<NBTTagFloat12>(if (MinecraftVersion.isUniversal) "w" else "data")
    val nbtTagDoubleGetter = unreflectGetter<NBTTagDouble12>(if (MinecraftVersion.isUniversal) "w" else "data")
    val nbtTagStringGetter = unreflectGetter<NBTTagString12>(if (MinecraftVersion.isUniversal) "A" else "data")
    val nbtTagByteArrayGetter = unreflectGetter<NBTTagByteArray12>(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagIntArrayGetter = unreflectGetter<NBTTagIntArray12>(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagLongArrayGetter = unreflectGetter<NBTTagLongArray12>(if (MinecraftVersion.isUniversal) "c" else "b")

    private fun getNMSCopy(itemStack: ItemStack): NMSItemStack12 {
        return org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asNMSCopy(itemStack)
    }

    private fun getBukkitCopy(itemStack: net.minecraft.server.v1_12_R1.ItemStack): ItemStack {
        return org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asBukkitCopy(itemStack)
    }

    override fun getItemTag(itemStack: ItemStack): ItemTag {
        val nmsItem = getNMSCopy(itemStack)
        return if (nmsItem.hasTag()) itemTagToBukkitCopy(nmsItem.tag!!).asCompound() else ItemTag()
    }

    override fun setItemTag(itemStack: ItemStack, itemTag: ItemTag): ItemStack {
        val nmsItem = getNMSCopy(itemStack)
        nmsItem.tag = itemTagToNMSCopy(itemTag) as NBTTagCompound12
        return getBukkitCopy(nmsItem)
    }

    override fun itemTagToString(itemTagData: ItemTagData): String {
        return itemTagToNMSCopy(itemTagData).toString()
    }

    override fun itemTagToNMSCopy(itemTagData: ItemTagData): Any {
        val new = MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_15)
        return when (itemTagData.type) {
            // 基本类型
            ItemTagType.BYTE -> if (new) NBTTagByte15.a(itemTagData.asByte()) else NBTTagByte12(itemTagData.asByte())
            ItemTagType.SHORT -> if (new) NBTTagShort15.a(itemTagData.asShort()) else NBTTagShort12(itemTagData.asShort())
            ItemTagType.INT -> if (new) NBTTagInt15.a(itemTagData.asInt()) else NBTTagInt12(itemTagData.asInt())
            ItemTagType.LONG -> if (new) NBTTagLong15.a(itemTagData.asLong()) else NBTTagLong12(itemTagData.asLong())
            ItemTagType.FLOAT -> if (new) NBTTagFloat15.a(itemTagData.asFloat()) else NBTTagFloat12(itemTagData.asFloat())
            ItemTagType.DOUBLE -> if (new) NBTTagDouble15.a(itemTagData.asDouble()) else NBTTagDouble12(itemTagData.asDouble())
            ItemTagType.STRING -> if (new) NBTTagString15.a(itemTagData.asString()) else NBTTagString12(itemTagData.asString())

            // 数组类型特殊处理
            ItemTagType.BYTE_ARRAY -> NBTTagByteArray12(itemTagData.asByteArray().copyOf())
            ItemTagType.INT_ARRAY -> NBTTagIntArray12(itemTagData.asIntArray().copyOf())
            ItemTagType.LONG_ARRAY -> NBTTagLongArray12(itemTagData.asLongArray().copyOf())

            // 列表类型特殊处理
            ItemTagType.LIST -> {
                NBTTagList12().also { nmsList ->
                    // 反射获取字段：
                    // private final List<NBTBase> list;
                    val list = nbtTagListGetter.get<MutableList<Any>>(nmsList)
                    val dataList = itemTagData.asList()
                    if (dataList.isNotEmpty()) {
                        dataList.forEach { list += itemTagToNMSCopy(it) }
                        // 修改 NBTTagList 的类型，不改他妈这条 List 作废，天坑。。。
                        nbtTagListTypeSetter.set(nmsList, dataList.first().type.id)
                    }
                }
            }

            // 复合类型特殊处理
            ItemTagType.COMPOUND -> {
                NBTTagCompound12().also { nmsCompound ->
                    // 反射获取字段：
                    // private final Map<String, NBTBase> map
                    val map = nbtTagCompoundGetter.get<MutableMap<String, Any>>(nmsCompound)
                    itemTagData.asCompound().entries.forEach { (key, value) -> map[key] = itemTagToNMSCopy(value) }
                }
            }

            // 不支持的类型
            else -> error("Unsupported type: ${itemTagData.type}}")
        }
    }

    override fun itemTagToBukkitCopy(nbtTag: Any): ItemTagData {
        return when (nbtTag) {
            // 基本类型
            is NBTTagByte12 -> ItemTagData(ItemTagType.BYTE, nbtTagByteGetter.get(nbtTag))
            is NBTTagShort12 -> ItemTagData(ItemTagType.SHORT, nbtTagShortGetter.get(nbtTag))
            is NBTTagInt12 -> ItemTagData(ItemTagType.INT, nbtTagIntGetter.get(nbtTag))
            is NBTTagLong12 -> ItemTagData(ItemTagType.LONG, nbtTagLongGetter.get(nbtTag))
            is NBTTagFloat12 -> ItemTagData(ItemTagType.FLOAT, nbtTagFloatGetter.get(nbtTag))
            is NBTTagDouble12 -> ItemTagData(ItemTagType.DOUBLE, nbtTagDoubleGetter.get(nbtTag))
            is NBTTagString12 -> ItemTagData(ItemTagType.STRING, nbtTagStringGetter.get(nbtTag))

            // 数组类型特殊处理
            is NBTTagByteArray12 -> ItemTagData(ItemTagType.BYTE_ARRAY, nbtTagByteArrayGetter.get<ByteArray>(nbtTag).copyOf())
            is NBTTagIntArray12 -> ItemTagData(ItemTagType.INT_ARRAY, nbtTagIntArrayGetter.get<IntArray>(nbtTag).copyOf())
            is NBTTagLongArray12 -> ItemTagData(ItemTagType.LONG_ARRAY, nbtTagLongArrayGetter.get<LongArray>(nbtTag).copyOf())

            // 列表类型特殊处理
            is NBTTagList12 -> {
                ItemTagList(nbtTagListGetter.get<List<Any>>(nbtTag).map { itemTagToBukkitCopy(it) })
            }

            // 复合类型特殊处理
            is NBTTagCompound12 -> {
                ItemTag().apply { nbtTagCompoundGetter.get<Map<String, Any>>(nbtTag).forEach { put(it.key, itemTagToBukkitCopy(it.value)) } }
            }

            // 不支持的类型
            else -> error("Unsupported type: ${nbtTag::class.java}}")
        }
    }

    private inline fun <reified T> unreflectGetter(name: String): MethodHandle {
        return UnsafeAccess.lookup.unreflectGetter(T::class.java.getDeclaredField(name).apply { isAccessible = true })
    }

    private inline fun <reified T> unreflectSetter(name: String): MethodHandle {
        return UnsafeAccess.lookup.unreflectSetter(T::class.java.getDeclaredField(name).apply { isAccessible = true })
    }

    private fun <T> MethodHandle.get(src: Any): T {
        return bindTo(src).invoke() as T
    }

    private fun <T> MethodHandle.set(src: Any, value: T) {
        bindTo(src).invoke(value)
    }
}

typealias NMSItemStack12 = net.minecraft.server.v1_12_R1.ItemStack
typealias NBTTagCompound12 = net.minecraft.server.v1_12_R1.NBTTagCompound
typealias NBTTagList12 = net.minecraft.server.v1_12_R1.NBTTagList
typealias NBTTagByte12 = net.minecraft.server.v1_12_R1.NBTTagByte
typealias NBTTagShort12 = net.minecraft.server.v1_12_R1.NBTTagShort
typealias NBTTagInt12 = net.minecraft.server.v1_12_R1.NBTTagInt
typealias NBTTagLong12 = net.minecraft.server.v1_12_R1.NBTTagLong
typealias NBTTagFloat12 = net.minecraft.server.v1_12_R1.NBTTagFloat
typealias NBTTagDouble12 = net.minecraft.server.v1_12_R1.NBTTagDouble
typealias NBTTagString12 = net.minecraft.server.v1_12_R1.NBTTagString
typealias NBTTagByteArray12 = net.minecraft.server.v1_12_R1.NBTTagByteArray
typealias NBTTagIntArray12 = net.minecraft.server.v1_12_R1.NBTTagIntArray
typealias NBTTagLongArray12 = net.minecraft.server.v1_12_R1.NBTTagLongArray

typealias NMSItemStack15 = net.minecraft.server.v1_15_R1.ItemStack
typealias NBTTagCompound15 = net.minecraft.server.v1_15_R1.NBTTagCompound
typealias NBTTagList15 = net.minecraft.server.v1_15_R1.NBTTagList
typealias NBTTagByte15 = net.minecraft.server.v1_15_R1.NBTTagByte
typealias NBTTagShort15 = net.minecraft.server.v1_15_R1.NBTTagShort
typealias NBTTagInt15 = net.minecraft.server.v1_15_R1.NBTTagInt
typealias NBTTagLong15 = net.minecraft.server.v1_15_R1.NBTTagLong
typealias NBTTagFloat15 = net.minecraft.server.v1_15_R1.NBTTagFloat
typealias NBTTagDouble15 = net.minecraft.server.v1_15_R1.NBTTagDouble
typealias NBTTagString15 = net.minecraft.server.v1_15_R1.NBTTagString
typealias NBTTagByteArray15 = net.minecraft.server.v1_15_R1.NBTTagByteArray
typealias NBTTagIntArray15 = net.minecraft.server.v1_15_R1.NBTTagIntArray
typealias NBTTagLongArray15 = net.minecraft.server.v1_15_R1.NBTTagLongArray