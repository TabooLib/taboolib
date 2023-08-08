package taboolib.module.nms

import com.google.gson.GsonBuilder
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
            getDeepWith(key) { it.remove(key.substringAfterLast('.')) }
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
            getDeepWith(key) { it.put(key.substringAfterLast('.'), toNBT(value)) }
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
            getDeepWith(key) { it[key.substringAfterLast('.')] }
        } else {
            get(key)
        }
    }

    /**
     * 针对 getDeep, putDeep, removeDeep 的重复代码做出的优化
     */
    fun getDeepWith(key: String, action: (ItemTag) -> ItemTagData?): ItemTagData? {
        val keys = key.split('.').dropLast(1)
        if (keys.isEmpty()) {
            return null
        }
        var find: ItemTag = this
        for (element in keys) {
            val next = find[element] ?: return null
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

    override fun toString(): String {
        return saveToString()
    }

    /*

    public static ItemTag fromJson(String json) {
        return fromJson(new JsonParser().parse(json)).asCompound();
    }

    public static ItemTagData fromJson(JsonElement element) {
        return ItemTagSerializer.INSTANCE.deserializeData(element);
    }

    @Deprecated
    public String toLegacyJson() {
        return new Gson().toJson(this);
    }

    @Deprecated
    public static ItemTag fromLegacyJson(String json) {
        return fromLegacyJson(new JsonParser().parse(json)).asCompound();
    }

    @Deprecated
    public static ItemTagData fromLegacyJson(JsonElement element) {
        if (element instanceof JsonObject) {
            JsonObject json = (JsonObject) element;
            // base
            if (json.has("type") && json.has("data") && json.entrySet().size() == 2) {
                switch (ItemTagType.parse(json.get("type").getAsString())) {
                    case BYTE:
                        return new ItemTagData(json.get("data").getAsByte());
                    case SHORT:
                        return new ItemTagData(json.get("data").getAsShort());
                    case INT:
                        return new ItemTagData(json.get("data").getAsInt());
                    case LONG:
                        return new ItemTagData(json.get("data").getAsLong());
                    case FLOAT:
                        return new ItemTagData(json.get("data").getAsFloat());
                    case DOUBLE:
                        return new ItemTagData(json.get("data").getAsDouble());
                    case STRING:
                        return new ItemTagData(json.get("data").getAsString());
                    case BYTE_ARRAY: {
                        JsonArray array = json.get("data").getAsJsonArray();
                        byte[] bytes = new byte[array.size()];
                        for (int i = 0; i < array.size(); i++) {
                            bytes[i] = array.get(i).getAsByte();
                        }
                        return new ItemTagData(bytes);
                    }
                    case INT_ARRAY: {
                        JsonArray array = json.get("data").getAsJsonArray();
                        int[] ints = new int[array.size()];
                        for (int i = 0; i < array.size(); i++) {
                            ints[i] = array.get(i).getAsInt();
                        }
                        return new ItemTagData(ints);
                    }
                    default: {
                        return new ItemTagData("error: " + element);
                    }
                }
            }
            // compound
            else {
                ItemTag compound = new ItemTag();
                for (Entry<String, JsonElement> elementEntry : json.entrySet()) {
                    compound.put(elementEntry.getKey(), fromLegacyJson(elementEntry.getValue()));
                }
                return compound;
            }
        }
        // list
        else if (element instanceof JsonArray) {
            ItemTagList list = new ItemTagList();
            for (JsonElement jsonElement : (JsonArray) element) {
                list.add(fromLegacyJson(jsonElement));
            }
            return list;
        }
        return new ItemTagData("error: " + element);
    }

     */
}