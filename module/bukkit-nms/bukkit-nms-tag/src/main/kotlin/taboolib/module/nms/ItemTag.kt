package taboolib.module.nms

import com.google.gson.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.ItemTagSerializer.serializeData
import taboolib.module.nms.ItemTagSerializer.serializeTag
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.ItemTag
 *
 * @author 坏黑
 * @since 2023/8/9 01:40
 */
open class ItemTag : ItemTagData, MutableMap<String, ItemTagData> {

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
     * 注意，此方法会修改传入的物品
     *
     * @param item 要写入的物品
     * @param onlyCustom 是否仅包含自定义数据（详见 1.20.5+ NBT 改动，在 1.20.4 及以下版本此参数无效）
     * @return 写入 [ItemTag] 后的物品（等价于传入的物品）
     */
    open fun saveTo(item: ItemStack, onlyCustom: Boolean = true): ItemStack {
        item.setItemMeta(item.setItemTag(this, onlyCustom).itemMeta)
        return item
    }

    /**
     * 转换为 Json 字符串（可逆向）
     * @return Json 字符串
     */
    fun toJson(): String {
        return serializeData(this).toString()
    }

    /**
     * 转换为格式化的 Json 字符串（可逆向）
     * @return 格式化的 Json 字符串
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
     * 深度删除指定键对应的数据
     *
     * 使用 "." 作为分层符来指定嵌套结构中的键
     * 例如 "a.b.c" 表示删除 a 对象中 b 对象中的 c 键
     *
     * @param key 要删除的键,可以包含 "." 来指定嵌套路径
     * @return 被删除的数据,如果键不存在则返回 null
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

    fun put(key: String, value: Any?): ItemTagData? {
        return if (value == null) {
            remove(key)
        } else {
            this.value.put(key, toNBT(value))
        }
    }

    /**
     * 深度写入指定键对应的数据
     *
     * 使用 "." 作为分层符来指定嵌套结构中的键
     * 例如 "a.b.c" 表示在 a 对象中的 b 对象中写入 c 键
     *
     * @param key 要写入的键，可以包含 "." 来指定嵌套路径
     * @param value 要写入的值，如果为 null 则删除该键
     * @return 被替换的原有数据，如果是新增键则返回 null
     */
    fun putDeep(key: String, value: Any?): ItemTagData? {
        return if (value == null) {
            removeDeep(key)
        } else if (key.contains('.')) {
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
     * 深度获取指定键对应的数据
     *
     * 使用 "." 作为分层符来指定嵌套结构中的键
     * 例如 "a.b.c" 表示获取 a 对象中的 b 对象中的 c 键对应的值
     *
     * @param key 要获取的键，可以包含 "." 来指定嵌套路径
     * @return 指定键对应的数据，如果不存在则返回 null
     */
    fun getDeep(key: String): ItemTagData? {
        return if (key.contains('.')) {
            getDeepWith(key, false) { it[key.substringAfterLast('.')] }
        } else {
            get(key)
        }
    }

    /**
     * 针对 getDeep、putDeep 和 removeDeep 方法的内部辅助函数。
     *
     * 该函数通过递归遍历嵌套的 ItemTag 结构，找到或创建指定路径的 ItemTag，然后执行给定的操作。
     *
     * @param key 以点号分隔的键路径，例如 "a.b.c"
     * @param create 如果为 true，在路径不存在时会创建新的 ItemTag；如果为 false，遇到不存在的路径会返回 null
     * @param action 在找到或创建的最终 ItemTag 上执行的操作
     * @return 执行 action 后的结果，可能为 null
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
                    next = empty()
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
     * 深度获取指定键对应的值，支持使用 "." 作为分层符来指定嵌套结构中的键。
     *
     * 如果指定的键不存在，则返回提供的默认值。
     *
     * @param key 要获取的键，可以包含 "." 来指定嵌套路径，例如 "a.b.c"
     * @param base 当指定的键不存在时返回的默认值
     * @return 指定键对应的数据，如果不存在则返回默认值
     */
    fun getDeepOrElse(key: String, base: ItemTagData): ItemTagData {
        return getDeep(key) ?: base
    }

    /**
     * 深度设置指定键对应的值, 支持使用 "." 作为分层符来指定嵌套结构中的键。
     *
     * 如果指定的键不存在, 则会自动创建。
     *
     * @param key 要设置的键，可以包含 "." 来指定嵌套路径，例如 "a.b.c"
     * @param value 要设置的值
     */
    operator fun set(key: String, value: Any?) {
        if (value == null) {
            removeDeep(key)
        } else {
            if (value is UUID || value is Boolean) {
                putDeep(key, value.toString())
                return
            }
            putDeep(key, value)
        }
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

        fun empty(): ItemTag {
            return NMSItemTag.instance.newItemTag()
        }

        /**
         * 将物品转换为 JSON 字符串。
         *
         * 此方法将给定的 [ItemStack] 对象转换为 JSON 格式的字符串表示。
         * 转换后的 JSON 字符串可以用于存储或传输物品数据。
         *
         * 在 1.20.5+，此方法会包含物品的材质、数量等基础信息。
         *
         * @param item 要转换的物品堆
         * @return 表示物品的 JSON 字符串
         */
        @JvmStatic
        fun toJson(item: ItemStack): String {
            return item.getItemTag(onlyCustom = false).toJson()
        }

        /**
         * 从 JSON 字符串创建物品堆。
         *
         * 此方法将给定的 JSON 格式字符串解析并创建对应的 [ItemStack] 对象。
         * 它是 [toJson] 方法的逆操作。
         *
         * 在 1.20.5+，此方法要求 JSON 字符串中包含物品的材质、数量等基础信息。
         * 在低版本，此方法产出的物品默认为 STONE 类型。
         *
         * @param json 表示物品的 JSON 字符串
         * @return 从 JSON 创建的 [ItemStack] 对象
         */
        @JvmStatic
        fun toItem(json: String): ItemStack {
            return fromJson(json).saveTo(ItemStack(Material.STONE))
        }

        /**
         * 从 JSON 字符串创建 [ItemTag] 对象。
         *
         * @param json 要解析的 JSON 字符串
         * @return 解析后的 [ItemTag] 对象
         */
        @JvmStatic
        fun fromJson(json: String): ItemTag {
            return fromJson(JsonParser().parse(json)).asCompound()
        }

        /**
         * 从 [JsonElement] 创建 [ItemTagData] 对象。
         *
         * @param element 要解析的 [JsonElement]
         * @return 解析后的 [ItemTagData] 对象
         */
        @JvmStatic
        fun fromJson(element: JsonElement): ItemTagData {
            return ItemTagSerializer.deserializeData(element)
        }

        // region Legacy Version

        /**
         * 从旧版 JSON 字符串创建 [ItemTag] 对象。
         *
         * 旧版 JSON 格式示例：
         * ```json
         * {
         *   "type": "INT",
         *   "data": 42
         * }
         * ```
         * 或
         * ```json
         * {
         *   "key1": {"type": "STRING", "data": "value1"},
         *   "key2": {"type": "INT", "data": 10}
         * }
         * ```
         *
         * @param json 要解析的旧版 JSON 字符串
         * @return 解析后的 [ItemTag] 对象
         */
        @JvmStatic
        fun fromLegacyJson(json: String): ItemTag {
            return fromLegacyJson(JsonParser().parse(json)).asCompound()
        }

        /**
         * 从旧版 JSON 格式的 [JsonElement] 创建 [ItemTagData] 对象。
         *
         * 此方法用于解析旧版 JSON 格式的数据，支持基本类型和复合类型。
         * 基本类型的 JSON 格式示例：
         * ```json
         * {
         *   "type": "INT",
         *   "data": 42
         * }
         * ```
         * 复合类型的 JSON 格式示例：
         * ```json
         * {
         *   "key1": {"type": "STRING", "data": "value1"},
         *   "key2": {"type": "INT", "data": 10}
         * }
         * ```
         *
         * @param element 要解析的 [JsonElement]
         * @return 解析后的 [ItemTagData] 对象
         * @throws IllegalArgumentException 当遇到不支持的 NBT 类型时
         */
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
                        // long 数组
                        ItemTagType.LONG_ARRAY -> {
                            val array = json.get("data").asJsonArray
                            val longs = LongArray(array.size())
                            for (i in 0 until array.size()) {
                                longs[i] = array.get(i).asLong
                            }
                            ItemTagData(longs)
                        }
                        // 不支持的类型
                        else -> error("Unsupported nbt type: $type")
                    }
                } else {
                    // 复合类型
                    val compound = empty()
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

        // endregion
    }
}