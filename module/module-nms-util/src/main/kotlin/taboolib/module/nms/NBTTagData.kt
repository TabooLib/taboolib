package taboolib.module.nms

import org.bukkit.configuration.ConfigurationSection
import taboolib.common5.*
import java.util.regex.Pattern

/**
 * TabooLib
 * taboolib.module.nms.NBTTagData
 *
 * @author 坏黑
 * @since 2023/8/9 01:01
 */
open class NBTTagData(val type: NBTTagType, protected var data: Any) {

    constructor(data: Byte) : this(NBTTagType.BYTE, data)

    constructor(data: Short) : this(NBTTagType.SHORT, data)

    constructor(data: Int) : this(NBTTagType.INT, data)

    constructor(data: Long) : this(NBTTagType.LONG, data)

    constructor(data: Float) : this(NBTTagType.FLOAT, data)

    constructor(data: Double) : this(NBTTagType.DOUBLE, data)

    constructor(data: ByteArray) : this(NBTTagType.BYTE_ARRAY, data)

    constructor(data: String) : this(NBTTagType.STRING, data)

    constructor(data: NBTTagList) : this(NBTTagType.LIST, data)

    constructor(data: NBTTag) : this(NBTTagType.COMPOUND, data)

    constructor(data: IntArray) : this(NBTTagType.INT_ARRAY, data)

    constructor(data: LongArray) : this(NBTTagType.LONG_ARRAY, data)

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
     * 获取为 [NBTTagList]
     */
    open fun asList(): NBTTagList = if (data is NBTTagList) data as NBTTagList else NBTTagList.of(data)

    /**
     * 获取为 [NBTTag]
     */
    open fun asCompound() = data as NBTTag

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
     * 克隆 [NBTTag]
     */
    open fun clone(): NBTTagData {
        return when (type) {
            NBTTagType.END -> NBTTagData(type, 0)
            // 基本类型
            NBTTagType.BYTE,
            NBTTagType.SHORT,
            NBTTagType.INT,
            NBTTagType.LONG,
            NBTTagType.FLOAT,
            NBTTagType.DOUBLE,
            NBTTagType.STRING -> NBTTagData(type, unsafeData())
            // 数组和列表需要深拷贝
            NBTTagType.BYTE_ARRAY -> NBTTagData(type, asByteArray().copyOf())
            NBTTagType.INT_ARRAY -> NBTTagData(type, asIntArray().copyOf())
            NBTTagType.LONG_ARRAY -> NBTTagData(type, asLongArray().copyOf())
            NBTTagType.LIST -> NBTTagList().also { list -> asList().forEach { list.add(it.clone()) } }
            NBTTagType.COMPOUND -> NBTTag().also { compound -> asCompound().forEach { (k, v) -> compound[k] = v.clone() } }
        }
    }

    /**
     * 转换为字符串
     */
    override fun toString(): String {
        return saveToString()
    }

    companion object {

        private val shortPattern: Pattern = Pattern.compile("\\d+s")

        @JvmStatic
        fun toNBT(obj: Any?): NBTTagData {
            obj ?: return NBTTagData(NBTTagType.END, 0)
            return when (obj) {
                is NBTTagData -> obj
                // 在字符串类型中对 1s 这种特殊数字类型进行处理
                is String -> if (shortPattern.matcher(obj).matches()) NBTTagData(obj.substring(0, obj.length - 1).cshort) else NBTTagData(obj)
                is Int -> NBTTagData(obj)
                is Double -> NBTTagData(obj)
                is Float -> NBTTagData(obj)
                is Short -> NBTTagData(obj)
                is Long -> NBTTagData(obj)
                is Byte -> NBTTagData(obj)
                is ByteArray -> NBTTagData(obj)
                is IntArray -> NBTTagData(obj)
                is LongArray -> NBTTagData(obj)
                is List<*> -> translateList(NBTTagList(), obj)
                is Map<*, *> -> NBTTag(obj.map { (k, v) -> k.toString() to toNBT(v) }.toMap())
                is ConfigurationSection -> translateSection(NBTTag(), obj)
                else -> error("Unsupported nbt: $obj (${obj.javaClass})")
            }
        }

        @JvmStatic
        fun translateList(itemTagList: NBTTagList, anyList: List<*>): NBTTagList {
            itemTagList += anyList.map { toNBT(it!!) }
            return itemTagList
        }

        @JvmStatic
        fun translateSection(itemTag: NBTTag, bukkitSection: ConfigurationSection): NBTTag {
            bukkitSection.getKeys(false).forEach { key ->
                val value = bukkitSection[key]!!
                if (value is ConfigurationSection) {
                    itemTag[key] = translateSection(NBTTag(), bukkitSection.getConfigurationSection(key)!!)
                } else {
                    itemTag[key] = toNBT(value)
                }
            }
            return itemTag
        }
    }
}