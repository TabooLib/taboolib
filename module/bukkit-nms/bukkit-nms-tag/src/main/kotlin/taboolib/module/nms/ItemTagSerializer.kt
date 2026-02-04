package taboolib.module.nms

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.bukkit.util.NumberConversions

/**
 * 序列化和反序列化 ItemTag 相关数据的工具方法
 */
object ItemTagSerializer {

    /**
     * 将 [ItemTag] 序列化为 [JsonObject]
     *
     * @param tag 要序列化的 ItemTag
     * @return 序列化后的 JsonObject
     */
    fun serializeTag(tag: ItemTag): JsonObject {
        return JsonObject().also { json -> tag.forEach { (k, v) -> json.add(k, serializeData(v)) } }
    }

    /**
     * 将 [ItemTagList] 序列化为 [JsonArray]
     *
     * @param tagList 要序列化的 ItemTagList
     * @return 序列化后的 JsonArray
     */
    fun serializeList(tagList: ItemTagList): JsonArray {
        return JsonArray().also { json -> tagList.forEach { json.add(serializeData(it)) } }
    }

    /**
     * 将 [ItemTagData] 序列化为 [JsonElement]
     *
     * @param tagData 要序列化的 ItemTagData
     * @return 序列化后的 JsonElement
     */
    fun serializeData(tagData: ItemTagData): JsonElement {
        return when (tagData.type) {
            ItemTagType.COMPOUND -> serializeTag(tagData.asCompound())
            ItemTagType.LIST -> serializeList(tagData.asList())
            ItemTagType.BYTE -> JsonPrimitive("${tagData.asByte()}b")
            ItemTagType.SHORT -> JsonPrimitive("${tagData.asShort()}s")
            ItemTagType.INT -> JsonPrimitive("${tagData.asInt()}i")
            ItemTagType.LONG -> JsonPrimitive("${tagData.asLong()}l")
            ItemTagType.FLOAT -> JsonPrimitive("${tagData.asFloat()}f")
            ItemTagType.DOUBLE -> JsonPrimitive("${tagData.asDouble()}d")
            ItemTagType.STRING, ItemTagType.END -> JsonPrimitive("${tagData.asString()}t")
            ItemTagType.INT_ARRAY -> JsonPrimitive("${tagData.asIntArray().joinToString(",") { it.toString() }}i]")
            ItemTagType.BYTE_ARRAY -> JsonPrimitive("${tagData.asByteArray().joinToString(",") { it.toString() }}b]")
            ItemTagType.LONG_ARRAY -> JsonPrimitive("${tagData.asLongArray().joinToString(",") { it.toString() }}l]")
        }
    }

    /**
     * 将 [JsonObject] 反序列化为 [ItemTag]
     *
     * @param json 要反序列化的 JsonObject
     * @return 反序列化后的 ItemTag
     */
    fun deserializeTag(json: JsonObject): ItemTag {
        val itemTag = ItemTag.empty()
        json.entrySet().forEach { itemTag[it.key] = deserializeData(it.value) }
        return itemTag
    }

    /**
     * 将 [JsonArray] 反序列化为 [ItemTagList]
     *
     * @param json 要反序列化的 JsonArray
     * @return 反序列化后的 ItemTagList
     */
    fun deserializeArray(json: JsonArray): ItemTagList {
        val itemTagList = ItemTagList()
        json.forEach { itemTagList.add(deserializeData(it)) }
        return itemTagList
    }

    /**
     * 将 [JsonElement] 反序列化为 [ItemTagData]
     *
     * @param json 要反序列化的 JsonElement
     * @return 反序列化后的 ItemTagData
     * @throws IllegalArgumentException 当遇到不支持的 JSON 类型时
     */
    fun deserializeData(json: JsonElement): ItemTagData {
        return when (json) {
            is JsonArray -> deserializeArray(json)
            is JsonObject -> deserializeTag(json)
            is JsonPrimitive -> {
                val str = json.asString
                if (str.endsWith("]")) {
                    when (val i = str.substring(str.length - 2, str.length - 1)) {
                        "b" -> ItemTagData(str.substring(0, str.length - 2).split(",").map { NumberConversions.toByte(it) }.toByteArray())
                        "i" -> ItemTagData(str.substring(0, str.length - 2).split(",").map { NumberConversions.toInt(it) }.toIntArray())
                        "l" -> ItemTagData(str.substring(0, str.length - 2).split(",").map { NumberConversions.toLong(it) }.toLongArray())
                        else -> error("Unsupported array $json ($i)")
                    }
                } else {
                    when (val i = str.substring(str.length - 1, str.length)) {
                        "b" -> ItemTagData(NumberConversions.toByte(str.substring(0, str.length - 1)))
                        "s" -> ItemTagData(NumberConversions.toShort(str.substring(0, str.length - 1)))
                        "i" -> ItemTagData(NumberConversions.toInt(str.substring(0, str.length - 1)))
                        "l" -> ItemTagData(NumberConversions.toLong(str.substring(0, str.length - 1)))
                        "f" -> ItemTagData(NumberConversions.toFloat(str.substring(0, str.length - 1)))
                        "d" -> ItemTagData(NumberConversions.toDouble(str.substring(0, str.length - 1)))
                        "t" -> ItemTagData(str.substring(0, str.length - 1))
                        else -> error("Unsupported type $json ($i)")
                    }
                }
            }

            else -> error("Unsupported json $json (${json.javaClass.simpleName})")
        }
    }
}