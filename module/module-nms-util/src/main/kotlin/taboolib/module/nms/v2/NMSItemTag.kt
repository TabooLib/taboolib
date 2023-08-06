package taboolib.module.nms.v2

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.tabooproject.reflex.UnsafeAccess
import taboolib.module.nms.*
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
 * 克隆 ItemTag
 */
fun ItemTagData.clone(): ItemTagData {
    return when (type) {
        ItemTagType.END -> ItemTagData(type, null)
        ItemTagType.BYTE,
        ItemTagType.SHORT,
        ItemTagType.INT,
        ItemTagType.LONG,
        ItemTagType.FLOAT,
        ItemTagType.DOUBLE,
        ItemTagType.STRING -> ItemTagData(type, unsafeData())
        // 数组和列表需要深拷贝
        ItemTagType.BYTE_ARRAY -> ItemTagData(type, asByteArray().copyOf())
        ItemTagType.INT_ARRAY -> ItemTagData(type, asIntArray().copyOf())
        ItemTagType.LIST -> ItemTagList().also { list -> asList().forEach { list.add(it.clone()) } }
        ItemTagType.COMPOUND -> ItemTag().also { compound -> asCompound().forEach { (k, v) -> compound[k] = v.clone() } }
        // 不支持的类型
        else -> error("Unsupported type.")
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

    val nbtTagCompoundGetter = net.minecraft.server.v1_12_R1.NBTTagCompound::class.java.unreflectGetter(if (MinecraftVersion.isUniversal) "x" else "map")
    val nbtTagListGetter = net.minecraft.server.v1_12_R1.NBTTagList::class.java.unreflectGetter(if (MinecraftVersion.isUniversal) "c" else "list")
    val nbtTagByteGetter = net.minecraft.server.v1_12_R1.NBTTagByte::class.java.unreflectGetter(if (MinecraftVersion.isUniversal) "x" else "data")
    val nbtTagShortGetter = net.minecraft.server.v1_12_R1.NBTTagShort::class.java.unreflectGetter(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagIntGetter = net.minecraft.server.v1_12_R1.NBTTagInt::class.java.unreflectGetter(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagLongGetter = net.minecraft.server.v1_12_R1.NBTTagLong::class.java.unreflectGetter(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagFloatGetter = net.minecraft.server.v1_12_R1.NBTTagFloat::class.java.unreflectGetter(if (MinecraftVersion.isUniversal) "w" else "data")
    val nbtTagDoubleGetter = net.minecraft.server.v1_12_R1.NBTTagDouble::class.java.unreflectGetter(if (MinecraftVersion.isUniversal) "w" else "data")
    val nbtTagStringGetter = net.minecraft.server.v1_12_R1.NBTTagString::class.java.unreflectGetter(if (MinecraftVersion.isUniversal) "A" else "data")
    val nbtTagByteArrayGetter = net.minecraft.server.v1_12_R1.NBTTagByteArray::class.java.unreflectGetter(if (MinecraftVersion.isUniversal) "c" else "data")
    val nbtTagIntArrayGetter = net.minecraft.server.v1_12_R1.NBTTagIntArray::class.java.unreflectGetter(if (MinecraftVersion.isUniversal) "c" else "data")

    override fun getItemTag(itemStack: ItemStack): ItemTag {
        val nmsItem = nmsProxy<NMSItem>().getNMSCopy(itemStack) as net.minecraft.server.v1_12_R1.ItemStack
        return if (nmsItem.hasTag()) itemTagToBukkitCopy(nmsItem.tag!!).asCompound() else ItemTag()
    }

    override fun setItemTag(itemStack: ItemStack, itemTag: ItemTag): ItemStack {
        val nmsProxy = nmsProxy<NMSItem>()
        val nmsItem = nmsProxy.getNMSCopy(itemStack) as net.minecraft.server.v1_12_R1.ItemStack
        nmsItem.tag = itemTagToNMSCopy(itemTag) as net.minecraft.server.v1_12_R1.NBTTagCompound
        return nmsProxy.getBukkitCopy(nmsItem)
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
                    itemTagData.asList().forEach { list += itemTagToNMSCopy(it) }
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
                ItemTagData(ItemTagType.LIST, ItemTagList(nbtTagListGetter.get<List<Any>>(nbtTag).map { itemTagToBukkitCopy(it) }))
            }

            // 复合类型特殊处理
            is net.minecraft.server.v1_12_R1.NBTTagCompound -> {
                ItemTagData(ItemTagType.COMPOUND, ItemTag().apply { nbtTagCompoundGetter.get<Map<String, Any>>(nbtTag).forEach { put(it.key, itemTagToBukkitCopy(it.value)) } })
            }

            // 不支持的类型
            else -> error("Unsupported type: ${nbtTag::class.java}}")
        }
    }

    private fun Class<*>.unreflectGetter(name: String): MethodHandle {
        return UnsafeAccess.lookup.unreflectGetter(getDeclaredField(name).apply { isAccessible = true })
    }

    private fun <T> MethodHandle.get(src: Any): T {
        return bindTo(src).invoke() as T
    }
}