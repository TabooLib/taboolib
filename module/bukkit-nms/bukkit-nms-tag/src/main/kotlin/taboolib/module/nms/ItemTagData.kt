package taboolib.module.nms

import org.bukkit.configuration.ConfigurationSection
import taboolib.common5.*
import java.util.regex.Pattern

/**
 * TabooLib
 * taboolib.module.nms.ItemTagData
 *
 * @author 坏黑
 * @since 2023/8/9 01:01
 */
open class ItemTagData(val type: ItemTagType, protected var data: Any) {

    constructor(data: Byte) : this(ItemTagType.BYTE, data)

    constructor(data: Short) : this(ItemTagType.SHORT, data)

    constructor(data: Int) : this(ItemTagType.INT, data)

    constructor(data: Long) : this(ItemTagType.LONG, data)

    constructor(data: Float) : this(ItemTagType.FLOAT, data)

    constructor(data: Double) : this(ItemTagType.DOUBLE, data)

    constructor(data: ByteArray) : this(ItemTagType.BYTE_ARRAY, data)

    constructor(data: String) : this(ItemTagType.STRING, data)

    constructor(data: ItemTagList) : this(ItemTagType.LIST, data)

    constructor(data: ItemTag) : this(ItemTagType.COMPOUND, data)

    constructor(data: IntArray) : this(ItemTagType.INT_ARRAY, data)

    constructor(data: LongArray) : this(ItemTagType.LONG_ARRAY, data)

    /**
     * 获取为 [Byte]
     */
    open fun asByte() = data.cbyte

    /**
     * 获取为 [Short]
     */
    open fun asShort() = data.cshort

    /**
     * 获取为 [Int]
     */
    open fun asInt() = data.cint

    /**
     * 获取为 [Long]
     */
    open fun asLong() = data.clong

    /**
     * 获取为 [Float]
     */
    open fun asFloat() = data.cfloat

    /**
     * 获取为 [Double]
     */
    open fun asDouble() = data.cdouble

    /**
     * 获取为 [ByteArray]
     */
    open fun asByteArray() = data as ByteArray

    /**
     * 获取为 [String]
     */
    open fun asString() = data.toString()

    /**
     * 获取为 [ItemTagList]
     */
    open fun asList(): ItemTagList = if (data is ItemTagList) data as ItemTagList else ItemTagList.of(data)

    /**
     * 获取为 [ItemTag]
     */
    open fun asCompound() = data as ItemTag

    /**
     * 获取为 [IntArray]
     */
    open fun asIntArray() = data as IntArray

    /**
     * 获取为 [LongArray]
     */
    open fun asLongArray() = data as LongArray

    /**
     * 通过不安全的方式获取数据
     */
    open fun unsafeData() = data

    /**
     * 转换为可读的 Json（不可逆向）
     */
    open fun toJsonSimplified(): String {
        return toJsonSimplified(0)
    }

    /**
     * 转换为可读的 Json（不可逆向）
     */
    open fun toJsonSimplified(index: Int): String {
        return if (data is String) "\"" + data + "\"" else toString()
    }

    /**
     * 克隆 [ItemTag]
     */
    open fun clone(): ItemTagData {
        return when (type) {
            ItemTagType.END -> ItemTagData(type, 0)
            // 基本类型
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
            ItemTagType.LONG_ARRAY -> ItemTagData(type, asLongArray().copyOf())
            ItemTagType.LIST -> ItemTagList().also { list -> asList().forEach { list.add(it.clone()) } }
            ItemTagType.COMPOUND -> ItemTag.empty().also { compound -> asCompound().forEach { (k, v) -> compound[k] = v.clone() } }
        }
    }

    /**
     * 转换为字符串
     */
    override fun toString(): String {
        return saveToString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ItemTagData) return false
        return data == other.data
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object {

        private val shortPattern: Pattern = Pattern.compile("\\d+s")

        @JvmStatic
        fun toNBT(obj: Any?): ItemTagData {
            obj ?: return ItemTagData(ItemTagType.END, 0)
            return when (obj) {
                is ItemTagData -> obj
                // 在字符串类型中对 1s 这种特殊数字类型进行处理
                is String -> if (shortPattern.matcher(obj).matches()) ItemTagData(obj.substring(0, obj.length - 1).cshort) else ItemTagData(obj)
                is Int -> ItemTagData(obj)
                is Double -> ItemTagData(obj)
                is Float -> ItemTagData(obj)
                is Short -> ItemTagData(obj)
                is Long -> ItemTagData(obj)
                is Byte -> ItemTagData(obj)
                is ByteArray -> ItemTagData(obj)
                is IntArray -> ItemTagData(obj)
                is LongArray -> ItemTagData(obj)
                is List<*> -> translateList(ItemTagList(), obj)
                is Map<*, *> -> ItemTag.empty().also { compound -> obj.forEach { (k, v) -> compound[k.toString()] = toNBT(v) } }
                is ConfigurationSection -> translateSection(ItemTag.empty(), obj)
                else -> error("Unsupported nbt: $obj (${obj.javaClass})")
            }
        }

        @JvmStatic
        fun translateList(itemTagList: ItemTagList, anyList: List<*>): ItemTagList {
            itemTagList += anyList.map { toNBT(it!!) }
            return itemTagList
        }

        @JvmStatic
        fun translateSection(itemTag: ItemTag, bukkitSection: ConfigurationSection): ItemTag {
            bukkitSection.getKeys(false).forEach { key ->
                val value = bukkitSection[key]!!
                if (value is ConfigurationSection) {
                    itemTag[key] = translateSection(ItemTag.empty(), bukkitSection.getConfigurationSection(key)!!)
                } else {
                    itemTag[key] = toNBT(value)
                }
            }
            return itemTag
        }
    }
}