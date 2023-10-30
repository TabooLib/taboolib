package taboolib.module.nms

import com.google.gson.*
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.NBTTagSerializer.serializeData
import taboolib.module.nms.NBTTagSerializer.serializeTag
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.NBTTag
 *
 * @author 坏黑
 * @since 2023/8/9 01:40
 */
class NBTTag : NBTTagData, MutableMap<String, NBTTagData> {

    private val value = ConcurrentHashMap<String, NBTTagData>()

    constructor() : super(NBTTagType.COMPOUND, 0) {
        this.data = this
    }

    constructor(map: Map<String, NBTTagData>) : super(NBTTagType.COMPOUND, 0) {
        this.data = this
        this.value += map
    }

    /**
     * 将 [NBTTag] 写入物品
     */
    fun saveTo(item: ItemStack) {
        item.setItemMeta(item.setItemTag(this).itemMeta)
    }

    /**
     * 转换为 Json（可逆向）
     */
    fun toJson(): String {
        return serializeData(this).toString()
    }

    /**
     * 转换为格式化的 Json（可逆向）
     */
    fun toJsonFormatted(): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(serializeTag(this))
    }

    override fun asCompound(): NBTTag {
        return this
    }

    override fun toJsonSimplified(): String {
        return toJsonSimplified(0)
    }

    override fun toJsonSimplified(index: Int): String {
        val builder = StringBuilder()
        builder.append("{\n")
        value.forEach { (k: String?, v: NBTTagData) ->
            builder.append("  ".repeat(index + 1))
                .append("\"")
                .append(k)
                .append("\"")
                .append(": ")
                .append(v.toJsonSimplified(index + 1))
                .append("\n")
        }
        builder.append("  ".repeat(index)).append("}")
        return builder.toString()
    }

    override val entries: MutableSet<MutableMap.MutableEntry<String, NBTTagData>>
        get() = value.entries

    override val keys: MutableSet<String>
        get() = value.keys

    override val size: Int
        get() = value.size

    override val values: MutableCollection<NBTTagData>
        get() = value.values

    override fun clear() {
        value.clear()
    }

    override fun isEmpty(): Boolean {
        return value.isEmpty()
    }

    override fun remove(key: String): NBTTagData? {
        return value.remove(key)
    }

    /**
     * 深度删除，以 "." 作为分层符
     */
    fun removeDeep(key: String): NBTTagData? {
        return if (key.contains('.')) {
            getDeepWith(key, false) { it.remove(key.substringAfterLast('.')) }
        } else {
            remove(key)
        }
    }

    override fun putAll(from: Map<out String, NBTTagData>) {
        value += from
    }

    override fun put(key: String, value: NBTTagData): NBTTagData? {
        return this.value.put(key, value)
    }

    fun put(key: String, value: Any?): NBTTagData? {
        return if (value == null) {
            remove(key)
        } else {
            this.value.put(key, toNBT(value))
        }
    }

    /**
     * 深度写入，以 "." 作为分层符
     */
    fun putDeep(key: String, value: Any?): NBTTagData? {
        return if (value == null) {
            removeDeep(key)
        } else if (key.contains('.')) {
            getDeepWith(key, true) { it.put(key.substringAfterLast('.'), toNBT(value)) }
        } else {
            put(key, value)
        }
    }

    override fun get(key: String): NBTTagData? {
        return value[key]
    }

    fun getOrElse(key: String, base: NBTTagData): NBTTagData {
        return value.getOrDefault(key, base)
    }

    /**
     * 深度获取，以 "." 作为分层符
     */
    fun getDeep(key: String): NBTTagData? {
        return if (key.contains('.')) {
            getDeepWith(key, false) { it[key.substringAfterLast('.')] }
        } else {
            get(key)
        }
    }

    /**
     * 针对 getDeep, putDeep, removeDeep 的重复代码做出的优化
     */
    fun getDeepWith(key: String, create: Boolean, action: (NBTTag) -> NBTTagData?): NBTTagData? {
        val keys = key.split('.').dropLast(1)
        if (keys.isEmpty()) {
            return null
        }
        var find: NBTTag = this
        for (element in keys) {
            var next = find[element]
            if (next == null) {
                if (create) {
                    next = NBTTag()
                    find[element] = next
                } else {
                    return null
                }
            }
            if (next.type == NBTTagType.COMPOUND) {
                find = next.asCompound()
            } else {
                return null
            }
        }
        return action(find)
    }

    /**
     * 深度获取，以 "." 作为分层符，并支持默认值
     */
    fun getDeepOrElse(key: String, base: NBTTagData): NBTTagData {
        return getDeep(key) ?: base
    }

    override fun containsValue(value: NBTTagData): Boolean {
        return this.value.containsValue(value)
    }

    override fun containsKey(key: String): Boolean {
        return value.containsKey(key)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NBTTag) return false
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return saveToString()
    }

    @Deprecated("不推荐使用", ReplaceWith("Gson().toJson(this)", "com.google.gson.Gson"))
    fun toLegacyJson(): String {
        return Gson().toJson(this)
    }

    companion object {

        @JvmStatic
        fun fromJson(json: String): NBTTag {
            return fromJson(JsonParser().parse(json)).asCompound()
        }

        @JvmStatic
        fun fromJson(element: JsonElement): NBTTagData {
            return NBTTagSerializer.deserializeData(element)
        }

        @JvmStatic
        fun fromLegacyJson(json: String): NBTTag {
            return fromLegacyJson(JsonParser().parse(json)).asCompound()
        }

        @JvmStatic
        fun fromLegacyJson(element: JsonElement): NBTTagData {
            return if (element is JsonObject) {
                val json = element.asJsonObject
                // 基本类型
                if (json.has("type") && json.has("data") && json.entrySet().size == 2) {
                    when (val type = NBTTagType.parse(json.get("type").asString)) {
                        NBTTagType.BYTE -> NBTTagData(json.get("data").asByte)
                        NBTTagType.SHORT -> NBTTagData(json.get("data").asShort)
                        NBTTagType.INT -> NBTTagData(json.get("data").asInt)
                        NBTTagType.LONG -> NBTTagData(json.get("data").asLong)
                        NBTTagType.FLOAT -> NBTTagData(json.get("data").asFloat)
                        NBTTagType.DOUBLE -> NBTTagData(json.get("data").asDouble)
                        NBTTagType.STRING -> NBTTagData(json.get("data").asString)
                        // byte 数组
                        NBTTagType.BYTE_ARRAY -> {
                            val array = json.get("data").asJsonArray
                            val bytes = ByteArray(array.size())
                            for (i in 0 until array.size()) {
                                bytes[i] = array.get(i).asByte
                            }
                            NBTTagData(bytes)
                        }
                        // int 数组
                        NBTTagType.INT_ARRAY -> {
                            val array = json.get("data").asJsonArray
                            val ints = IntArray(array.size())
                            for (i in 0 until array.size()) {
                                ints[i] = array.get(i).asInt
                            }
                            NBTTagData(ints)
                        }
                        // long 数组
                        NBTTagType.LONG_ARRAY -> {
                            val array = json.get("data").asJsonArray
                            val longs = LongArray(array.size())
                            for (i in 0 until array.size()) {
                                longs[i] = array.get(i).asLong
                            }
                            NBTTagData(longs)
                        }
                        // 不支持的类型
                        else -> error("Unsupported nbt type: $type")
                    }
                } else {
                    // 复合类型
                    val compound = NBTTag()
                    for ((key, value) in json.entrySet()) {
                        compound[key] = fromLegacyJson(value)
                    }
                    compound
                }
            } else if (element is JsonArray) {
                val list = NBTTagList()
                for (value in element) {
                    list.add(fromLegacyJson(value))
                }
                list
            } else {
                error("Not JsonObject")
            }
        }
    }
}