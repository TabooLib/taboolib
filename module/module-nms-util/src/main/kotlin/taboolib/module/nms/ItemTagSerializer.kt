package taboolib.module.nms

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.bukkit.util.NumberConversions

/**
 * TabooLib
 * taboolib.module.nms.ItemTagSerializer
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
object ItemTagSerializer {

    /**
     * 序列化 [ItemTag] 为 [JsonObject]
     */
    fun serializeTag(tag: ItemTag): JsonObject {
        return JsonObject().also { json -> tag.forEach { (k, v) -> json.add(k, serializeData(v)) } }
    }

    /**
     * 序列化 [ItemTagList] 为 [JsonArray]
     */
    fun serializeList(tagList: ItemTagList): JsonArray {
        return JsonArray().also { json -> tagList.forEach { json.add(serializeData(it)) } }
    }

    /**
     * 序列化 [ItemTagData] 为 [JsonElement]
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
        }
    }

    /**
     * 反序列化 [JsonObject] 为 [ItemTag]
     */
    fun deserializeTag(json: JsonObject): ItemTag {
        val itemTag = ItemTag()
        json.entrySet().forEach { itemTag[it.key] = deserializeData(it.value) }
        return itemTag
    }

    /**
     * 反序列化 [JsonArray] 为 [ItemTagList]
     */
    fun deserializeArray(json: JsonArray): ItemTagList {
        val itemTagList = ItemTagList()
        json.forEach { itemTagList.add(deserializeData(it)) }
        return itemTagList
    }

    /**
     * 反序列化 [JsonElement] 为 [ItemTagData]
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
                        else -> error("unsupported array $json ($i)")
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
                        else -> error("unsupported type $json ($i)")
                    }
                }
            }
            else -> error("unsupported json $json (${json.javaClass.simpleName})")
        }
    }
}