package taboolib.module.nms

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.tabooproject.reflex.UnsafeAccess
import java.lang.invoke.MethodHandle

typealias ItemTag = NBTTag
typealias ItemTagData = NBTTagData
typealias ItemTagList = NBTTagList
typealias ItemTagSerializer = NBTTagSerializer
typealias ItemTagType = NBTTagType

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
 * 将 [NBTTagData] 转换为字符串
 */
fun NBTTagData.saveToString(): String {
    return nmsProxy<NMSItemTag>().nbtTagToString(this)
}

/**
 * 将 [NBTTagData] 转换为 [net.minecraft.server] 下的 NBTTagCompound
 */
fun NBTTagData.toNMSTag(): Any {
    return nmsProxy<NMSItemTag>().nbtTagToNMSCopy(this)
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

    /** 将 [NBTTag] 转换为字符串 */
    abstract fun nbtTagToString(nbtTagData: NBTTagData): String

    /** 将 [NBTTagData] 转换为 [net.minecraft.server] 下的 NBTTagCompound */
    abstract fun nbtTagToNMSCopy(nbtTagData: NBTTagData): Any

    /** 将 [net.minecraft.server] 下的 NBTTag 转换为 [NBTTagData] */
    abstract fun nbtTagToBukkitCopy(nbtTag: Any): NBTTagData
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
    val nbtTagLongArrayGetter = unreflectGetter<net.minecraft.server.v1_12_R1.NBTTagLongArray>(if (MinecraftVersion.isUniversal) "c" else "data")

    override fun getItemTag(itemStack: ItemStack): ItemTag {
        val nmsItem = NMSItem.asNMSCopy(itemStack) as net.minecraft.server.v1_12_R1.ItemStack
        return if (nmsItem.hasTag()) nbtTagToBukkitCopy(nmsItem.tag!!).asCompound() else ItemTag()
    }

    override fun setItemTag(itemStack: ItemStack, itemTag: ItemTag): ItemStack {
        val nmsItem = NMSItem.asNMSCopy(itemStack) as net.minecraft.server.v1_12_R1.ItemStack
        nmsItem.tag = nbtTagToNMSCopy(itemTag) as net.minecraft.server.v1_12_R1.NBTTagCompound
        return NMSItem.asBukkitCopy(nmsItem)
    }

    override fun nbtTagToString(nbtTagData: NBTTagData): String {
        return nbtTagToNMSCopy(nbtTagData).toString()
    }

    override fun nbtTagToNMSCopy(nbtTagData: NBTTagData): Any {
        val new = MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_15)
        return when (nbtTagData.type) {
            // 基本类型
            NBTTagType.BYTE -> if (new) net.minecraft.server.v1_15_R1.NBTTagByte.a(nbtTagData.asByte()) else net.minecraft.server.v1_12_R1.NBTTagByte(nbtTagData.asByte())
            NBTTagType.SHORT -> if (new) net.minecraft.server.v1_15_R1.NBTTagShort.a(nbtTagData.asShort()) else net.minecraft.server.v1_12_R1.NBTTagShort(nbtTagData.asShort())
            NBTTagType.INT -> if (new) net.minecraft.server.v1_15_R1.NBTTagInt.a(nbtTagData.asInt()) else net.minecraft.server.v1_12_R1.NBTTagInt(nbtTagData.asInt())
            NBTTagType.LONG -> if (new) net.minecraft.server.v1_15_R1.NBTTagLong.a(nbtTagData.asLong()) else net.minecraft.server.v1_12_R1.NBTTagLong(nbtTagData.asLong())
            NBTTagType.FLOAT -> if (new) net.minecraft.server.v1_15_R1.NBTTagFloat.a(nbtTagData.asFloat()) else net.minecraft.server.v1_12_R1.NBTTagFloat(nbtTagData.asFloat())
            NBTTagType.DOUBLE -> if (new) net.minecraft.server.v1_15_R1.NBTTagDouble.a(nbtTagData.asDouble()) else net.minecraft.server.v1_12_R1.NBTTagDouble(nbtTagData.asDouble())
            NBTTagType.STRING -> if (new) net.minecraft.server.v1_15_R1.NBTTagString.a(nbtTagData.asString()) else net.minecraft.server.v1_12_R1.NBTTagString(nbtTagData.asString())

            // 数组类型特殊处理
            NBTTagType.BYTE_ARRAY -> net.minecraft.server.v1_12_R1.NBTTagByteArray(nbtTagData.asByteArray().copyOf())
            NBTTagType.INT_ARRAY -> net.minecraft.server.v1_12_R1.NBTTagIntArray(nbtTagData.asIntArray().copyOf())
            NBTTagType.LONG_ARRAY -> net.minecraft.server.v1_12_R1.NBTTagLongArray(nbtTagData.asLongArray().copyOf())

            // 列表类型特殊处理
            NBTTagType.LIST -> {
                net.minecraft.server.v1_12_R1.NBTTagList().also { nmsList ->
                    // 反射获取字段：
                    // private final List<NBTBase> list;
                    val list = nbtTagListGetter.get<MutableList<Any>>(nmsList)
                    val dataList = nbtTagData.asList()
                    if (dataList.isNotEmpty()) {
                        dataList.forEach { list += nbtTagToNMSCopy(it) }
                        // 修改 NBTTagList 的类型，不改他妈这条 List 作废，天坑。。。
                        nbtTagListTypeSetter.set(nmsList, dataList.first().type.id)
                    }
                }
            }

            // 复合类型特殊处理
            NBTTagType.COMPOUND -> {
                net.minecraft.server.v1_12_R1.NBTTagCompound().also { nmsCompound ->
                    // 反射获取字段：
                    // private final Map<String, NBTBase> map
                    val map = nbtTagCompoundGetter.get<MutableMap<String, Any>>(nmsCompound)
                    nbtTagData.asCompound().entries.forEach { (key, value) -> map[key] = nbtTagToNMSCopy(value) }
                }
            }

            // 不支持的类型
            else -> error("Unsupported type: ${nbtTagData.type}}")
        }
    }

    override fun nbtTagToBukkitCopy(nbtTag: Any): NBTTagData {
        return when (nbtTag) {
            // 基本类型
            is net.minecraft.server.v1_12_R1.NBTTagByte -> NBTTagData(NBTTagType.BYTE, nbtTagByteGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagShort -> NBTTagData(NBTTagType.SHORT, nbtTagShortGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagInt -> NBTTagData(NBTTagType.INT, nbtTagIntGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagLong -> NBTTagData(NBTTagType.LONG, nbtTagLongGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagFloat -> NBTTagData(NBTTagType.FLOAT, nbtTagFloatGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagDouble -> NBTTagData(NBTTagType.DOUBLE, nbtTagDoubleGetter.get(nbtTag))
            is net.minecraft.server.v1_12_R1.NBTTagString -> NBTTagData(NBTTagType.STRING, nbtTagStringGetter.get(nbtTag))

            // 数组类型特殊处理
            is net.minecraft.server.v1_12_R1.NBTTagByteArray -> NBTTagData(NBTTagType.BYTE_ARRAY, nbtTagByteArrayGetter.get<ByteArray>(nbtTag).copyOf())
            is net.minecraft.server.v1_12_R1.NBTTagIntArray -> NBTTagData(NBTTagType.INT_ARRAY, nbtTagIntArrayGetter.get<IntArray>(nbtTag).copyOf())
            is net.minecraft.server.v1_12_R1.NBTTagLongArray -> NBTTagData(NBTTagType.LONG_ARRAY, nbtTagLongArrayGetter.get<LongArray>(nbtTag).copyOf())

            // 列表类型特殊处理
            is net.minecraft.server.v1_12_R1.NBTTagList -> {
                NBTTagList(nbtTagListGetter.get<List<Any>>(nbtTag).map { nbtTagToBukkitCopy(it) })
            }

            // 复合类型特殊处理
            is net.minecraft.server.v1_12_R1.NBTTagCompound -> {
                NBTTag().apply { nbtTagCompoundGetter.get<Map<String, Any>>(nbtTag).forEach { put(it.key, nbtTagToBukkitCopy(it.value)) } }
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