package taboolib.module.nms

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.tabooproject.reflex.UnsafeAccess
import taboolib.common.platform.function.info
import java.lang.invoke.MethodHandle

/**
 * 获取物品 [ItemTag]
 */
fun ItemStack.getItemTag(): ItemTag {
    return nmsProxy<NMSItemTag>().getItemTag(validation())
}

/**
 * 将 [ItemTag] 写入物品（不会改变该物品）并返回一个新的物品
 */
fun ItemStack.setItemTag(itemTag: ItemTag): ItemStack {
    return nmsProxy<NMSItemTag>().setItemTag(validation(), itemTag)
}

/**
 * 将 [ItemTagData] 转换为字符串
 */
fun ItemTagData.saveToString(): String {
    return nmsProxy<NMSItemTag>().itemTagToString(this)
}

/**
 * TabooLib
 * taboolib.module.nms.NMSItemTag
 *
 * @author 坏黑
 * @since 2023/8/5 03:47
 */
abstract class NMSItemTag {

    /** 获取物品 [ItemTag] */
    abstract fun getItemTag(itemStack: ItemStack): ItemTag

    /** 将 [ItemTag] 写入物品（不会改变该物品）并返回一个新的物品 */
    abstract fun setItemTag(itemStack: ItemStack, itemTag: ItemTag): ItemStack

    /** 将 [ItemTag] 转换为字符串 */
    abstract fun itemTagToString(itemTagData: ItemTagData): String

    /** 将 [ItemTagData] 转换为 [net.minecraft.server] 下的 NBTTagCompound */
    abstract fun itemTagToNMSCopy(itemTagData: ItemTagData): Any

    /** 将 [net.minecraft.server] 下的 NBTTag 转换为 [ItemTagData] */
    abstract fun itemTagToBukkitCopy(nbtTag: Any): ItemTagData
}

/**
 * [NMSItemTag] 的实现类
 */
@Suppress("SpellCheckingInspection", "UNCHECKED_CAST")
class NMSItemTagImpl : NMSItemTag() {

    val nbtTagCompoundGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagCompound>(if (MinecraftVersion.isUniversal) "x" else "map")
    val nbtTagListGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagList>(if (MinecraftVersion.isUniversal) "c" else "list")
    val nbtTagListTypeSetter = unreflectSetter<net.minecraft.server.v1_12_R1.NBTTagList>(if (MinecraftVersion.isUniversal) "w" else "type")
    val nbtTagByteGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagByte>(if (MinecraftVersion.isUniversal) "x" else "data")
    val nbtTagShortGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagShort>(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagIntGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagInt>(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagLongGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagLong>(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagFloatGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagFloat>(if (MinecraftVersion.isUniversal) "w" else "data")
    val nbtTagDoubleGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagDouble>(if (MinecraftVersion.isUniversal) "w" else "data")
    val nbtTagStringGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagString>(if (MinecraftVersion.isUniversal) "A" else "data")
    val nbtTagByteArrayGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagByteArray>(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagIntArrayGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagIntArray>(if (MinecraftVersion.isUniversal) "c" else "data")

    override fun getItemTag(itemStack: ItemStack): ItemTag {
        val nmsItem = NMSItem.asNMSCopy(itemStack) as net.minecraft.server.v1_12_R1.ItemStack
        return if (nmsItem.hasTag()) itemTagToBukkitCopy(nmsItem.tag!!).asCompound() else ItemTag()
    }

    override fun setItemTag(itemStack: ItemStack, itemTag: ItemTag): ItemStack {
        val nmsItem = NMSItem.asNMSCopy(itemStack) as net.minecraft.server.v1_12_R1.ItemStack
        nmsItem.tag = itemTagToNMSCopy(itemTag) as net.minecraft.server.v1_12_R1.NBTTagCompound
        return NMSItem.asBukkitCopy(nmsItem)
    }

    override fun itemTagToString(itemTagData: ItemTagData): String {
        return itemTagToNMSCopy(itemTagData).toString()
    }

    override fun itemTagToNMSCopy(itemTagData: ItemTagData): Any {
        val new = MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_15)
        return when (itemTagData.type) {
            // 基本类型
            ItemTagType.BYTE -> if (new) net.minecraft.server.v1_15_R1.NBTTagByte.a(itemTagData.asByte()) else net.minecraft.server.v1_12_R1.NBTTagByte(itemTagData.asByte())
            ItemTagType.SHORT -> if (new) net.minecraft.server.v1_15_R1.NBTTagShort.a(itemTagData.asShort()) else net.minecraft.server.v1_12_R1.NBTTagShort(itemTagData.asShort())
            ItemTagType.INT -> if (new) net.minecraft.server.v1_15_R1.NBTTagInt.a(itemTagData.asInt()) else net.minecraft.server.v1_12_R1.NBTTagInt(itemTagData.asInt())
            ItemTagType.LONG -> if (new) net.minecraft.server.v1_15_R1.NBTTagLong.a(itemTagData.asLong()) else net.minecraft.server.v1_12_R1.NBTTagLong(itemTagData.asLong())
            ItemTagType.FLOAT -> if (new) net.minecraft.server.v1_15_R1.NBTTagFloat.a(itemTagData.asFloat()) else net.minecraft.server.v1_12_R1.NBTTagFloat(itemTagData.asFloat())
            ItemTagType.DOUBLE -> if (new) net.minecraft.server.v1_15_R1.NBTTagDouble.a(itemTagData.asDouble()) else net.minecraft.server.v1_12_R1.NBTTagDouble(itemTagData.asDouble())
            ItemTagType.STRING -> if (new) net.minecraft.server.v1_15_R1.NBTTagString.a(itemTagData.asString()) else net.minecraft.server.v1_12_R1.NBTTagString(itemTagData.asString())

            // 数组类型特殊处理
            ItemTagType.BYTE_ARRAY -> net.minecraft.server.v1_12_R1.NBTTagByteArray(itemTagData.asByteArray().copyOf())
            ItemTagType.INT_ARRAY -> net.minecraft.server.v1_12_R1.NBTTagIntArray(itemTagData.asIntArray().copyOf())

            // 列表类型特殊处理
            ItemTagType.LIST -> {
                net.minecraft.server.v1_12_R1.NBTTagList().also { nmsList ->
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
                net.minecraft.server.v1_12_R1.NBTTagCompound().also { nmsCompound ->
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
            is net.minecraft.server.v1_12_R1.NBTTagByte -> ItemTagData(ItemTagType.BYTE, nbtTagByteGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagShort -> ItemTagData(ItemTagType.SHORT, nbtTagShortGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagInt -> ItemTagData(ItemTagType.INT, nbtTagIntGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagLong -> ItemTagData(ItemTagType.LONG, nbtTagLongGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagFloat -> ItemTagData(ItemTagType.FLOAT, nbtTagFloatGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagDouble -> ItemTagData(ItemTagType.DOUBLE, nbtTagDoubleGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagString -> ItemTagData(ItemTagType.STRING, nbtTagStringGetter.get(nbtTag))

            // 数组类型特殊处理
            is net.minecraft.server.v1_12_R1.NBTTagByteArray -> ItemTagData(ItemTagType.BYTE_ARRAY, nbtTagByteArrayGetter.get<ByteArray>(nbtTag).copyOf())
            is net.minecraft.server.v1_12_R1.NBTTagIntArray -> ItemTagData(ItemTagType.INT_ARRAY, nbtTagIntArrayGetter.get<IntArray>(nbtTag).copyOf())

            // 列表类型特殊处理
            is net.minecraft.server.v1_12_R1.NBTTagList -> {
                ItemTagList(nbtTagListGetter.get<List<Any>>(nbtTag).map { itemTagToBukkitCopy(it) })
            }

            // 复合类型特殊处理
            is net.minecraft.server.v1_12_R1.NBTTagCompound -> {
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

/**
 * 判断物品是否为空
 */
private fun ItemStack?.validation(): ItemStack {
    if (this == null || type == Material.AIR || type.name.endsWith("_AIR")) {
        error("ItemStack must be not null.")
    } else {
        return this
    }
}