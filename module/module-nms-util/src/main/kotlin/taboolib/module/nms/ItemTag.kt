package taboolib.module.nms

import com.google.gson.*
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.ItemTagSerializer.serializeData
import taboolib.module.nms.ItemTagSerializer.serializeTag
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.ItemTag
 *
 * @author 坏黑
 * @since 2023/8/9 01:40
 */
class ItemTag : ItemTagData, MutableMap<String, ItemTagData> {

    private val value = ConcurrentHashMap<String, ItemTagData>()

    constructor() : super(ItemTagType.COMPOUND, 0) {
        this.data = this
    }

    constructor(map: Map<String, ItemTagData>) : super(ItemTagType.COMPOUND, 0) {
        this.data = this
        this.value += map
    }

    /**
     * 将 [ItemTag] 写入物品
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

    override fun asCompound(): ItemTag {
        return this
    }

    override fun toJsonSimplified(): String {
        return toJsonSimplified(0)
    }

    override fun toJsonSimplified(index: Int): String {
        val builder = StringBuilder()
        builder.append("{\n")
        value.forEach { (k: String?, v: ItemTagData) ->
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

    override val entries: MutableSet<MutableMap.MutableEntry<String, ItemTagData>>
        get() = value.entries

    override val keys: MutableSet<String>
        get() = value.keys

    override val size: Int
        get() = value.size

    override val values: MutableCollection<ItemTagData>
        get() = value.values

    override fun clear() {
        value.clear()
    }

    override fun isEmpty(): Boolean {
        return value.isEmpty()
    }

    override fun remove(key: String): ItemTagData? {
        return value.remove(key)
    }

    /**
     * 深度删除，以 "." 作为分层符
     */
    fun removeDeep(key: String): ItemTagData? {
        return if (key.contains('.')) {
            getDeepWith(key, false) { it.remove(key.substringAfterLast('.')) }
        } else {
            remove(key)
        }
    }

    override fun putAll(from: Map<out String, ItemTagData>) {
        value += from
    }

    override fun put(key: String, value: ItemTagData): ItemTagData? {
        return this.value.put(key, value)
    }

    fun put(key: String, value: Any): ItemTagData? {
        return this.value.put(key, toNBT(value))
    }

    /**
     * 深度写入，以 "." 作为分层符
     */
    fun putDeep(key: String, value: Any): ItemTagData? {
        return if (key.contains('.')) {
            getDeepWith(key, true) { it.put(key.substringAfterLast('.'), toNBT(value)) }
        } else {
            put(key, value)
        }
    }

    override fun get(key: String): ItemTagData? {
        return value[key]
    }

    fun getOrElse(key: String, base: ItemTagData): ItemTagData {
        return value.getOrDefault(key, base)
    }

    /**
     * 深度获取，以 "." 作为分层符
     */
    fun getDeep(key: String): ItemTagData? {
        return if (key.contains('.')) {
            getDeepWith(key, false) { it[key.substringAfterLast('.')] }
        } else {
            get(key)
        }
    }

    /**
     * 针对 getDeep, putDeep, removeDeep 的重复代码做出的优化
     */
    fun getDeepWith(key: String, create: Boolean, action: (ItemTag) -> ItemTagData?): ItemTagData? {
        val keys = key.split('.').dropLast(1)
        if (keys.isEmpty()) {
            return null
        }
        var find: ItemTag = this
        for (element in keys) {
            var next = find[element]
            if (next == null) {
                if (create) {
                    next = ItemTag()
                    find[element] = next
                } else {
                    return null
                }
            }
            if (next.type == ItemTagType.COMPOUND) {
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
    fun getDeepOrElse(key: String, base: ItemTagData): ItemTagData {
        return getDeep(key) ?: base
    }

    override fun containsValue(value: ItemTagData): Boolean {
        return this.value.containsValue(value)
    }

    override fun containsKey(key: String): Boolean {
        return value.containsKey(key)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ItemTag) return false
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
        fun fromJson(json: String): ItemTag {
            return fromJson(JsonParser().parse(json)).asCompound()
        }

        @JvmStatic
        fun fromJson(element: JsonElement): ItemTagData {
            return ItemTagSerializer.deserializeData(element)
        }

        @JvmStatic
        fun fromLegacyJson(json: String): ItemTag {
            return fromLegacyJson(JsonParser().parse(json)).asCompound()
        }

        @JvmStatic
        fun fromLegacyJson(element: JsonElement): ItemTagData {
            return if (element is JsonObject) {
                val json = element.asJsonObject
                // 基本类型
                if (json.has("type") && json.has("data") && json.entrySet().size == 2) {
                    when (val type = ItemTagType.parse(json.get("type").asString)) {
                        ItemTagType.BYTE -> ItemTagData(json.get("data").asByte)
                        ItemTagType.SHORT -> ItemTagData(json.get("data").asShort)
                        ItemTagType.INT -> ItemTagData(json.get("data").asInt)
                        ItemTagType.LONG -> ItemTagData(json.get("data").asLong)
                        ItemTagType.FLOAT -> ItemTagData(json.get("data").asFloat)
                        ItemTagType.DOUBLE -> ItemTagData(json.get("data").asDouble)
                        ItemTagType.STRING -> ItemTagData(json.get("data").asString)
                        // byte 数组
                        ItemTagType.BYTE_ARRAY -> {
                            val array = json.get("data").asJsonArray
                            val bytes = ByteArray(array.size())
                            for (i in 0 until array.size()) {
                                bytes[i] = array.get(i).asByte
                            }
                            ItemTagData(bytes)
                        }
                        // int 数组
                        ItemTagType.INT_ARRAY -> {
                            val array = json.get("data").asJsonArray
                            val ints = IntArray(array.size())
                            for (i in 0 until array.size()) {
                                ints[i] = array.get(i).asInt
                            }
                            ItemTagData(ints)
                        }
                        // 不支持的类型
                        else -> error("Unsupported nbt type: $type")
                    }
                } else {
                    // 复合类型
                    val compound = ItemTag()
                    for ((key, value) in json.entrySet()) {
                        compound[key] = fromLegacyJson(value)
                    }
                    compound
                }
            } else if (element is JsonArray) {
                val list = ItemTagList()
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