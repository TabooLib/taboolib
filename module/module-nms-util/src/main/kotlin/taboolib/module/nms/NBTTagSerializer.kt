package taboolib.module.nms

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.bukkit.util.NumberConversions

/**
 * TabooLib
 * taboolib.module.nms.NBTTagSerializer
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
object NBTTagSerializer {

    /**
     * 序列化 [NBTTag] 为 [JsonObject]
     */
    fun serializeTag(tag: NBTTag): JsonObject {
        return JsonObject().also { json -> tag.forEach { (k, v) -> json.add(k, serializeData(v)) } }
    }

    /**
     * 序列化 [NBTTagList] 为 [JsonArray]
     */
    fun serializeList(tagList: NBTTagList): JsonArray {
        return JsonArray().also { json -> tagList.forEach { json.add(serializeData(it)) } }
    }

    /**
     * 序列化 [NBTTagData] 为 [JsonElement]
     */
    fun serializeData(tagData: NBTTagData): JsonElement {
        return when (tagData.type) {
            NBTTagType.COMPOUND -> serializeTag(tagData.asCompound())
            NBTTagType.LIST -> serializeList(tagData.asList())
            NBTTagType.BYTE -> JsonPrimitive("${tagData.asByte()}b")
            NBTTagType.SHORT -> JsonPrimitive("${tagData.asShort()}s")
            NBTTagType.INT -> JsonPrimitive("${tagData.asInt()}i")
            NBTTagType.LONG -> JsonPrimitive("${tagData.asLong()}l")
            NBTTagType.FLOAT -> JsonPrimitive("${tagData.asFloat()}f")
            NBTTagType.DOUBLE -> JsonPrimitive("${tagData.asDouble()}d")
            NBTTagType.STRING, NBTTagType.END -> JsonPrimitive("${tagData.asString()}t")
            NBTTagType.INT_ARRAY -> JsonPrimitive("${tagData.asIntArray().joinToString(",") { it.toString() }}i]")
            NBTTagType.BYTE_ARRAY -> JsonPrimitive("${tagData.asByteArray().joinToString(",") { it.toString() }}b]")
            NBTTagType.LONG_ARRAY -> JsonPrimitive("${tagData.asLongArray().joinToString(",") { it.toString() }}l]")
        }
    }

    /**
     * 反序列化 [JsonObject] 为 [NBTTag]
     */
    fun deserializeTag(json: JsonObject): NBTTag {
        val itemTag = NBTTag()
        json.entrySet().forEach { itemTag[it.key] = deserializeData(it.value) }
        return itemTag
    }

    /**
     * 反序列化 [JsonArray] 为 [NBTTagList]
     */
    fun deserializeArray(json: JsonArray): NBTTagList {
        val itemTagList = NBTTagList()
        json.forEach { itemTagList.add(deserializeData(it)) }
        return itemTagList
    }

    /**
     * 反序列化 [JsonElement] 为 [NBTTagData]
     */
    fun deserializeData(json: JsonElement): NBTTagData {
        return when (json) {
            is JsonArray -> deserializeArray(json)
            is JsonObject -> deserializeTag(json)
            is JsonPrimitive -> {
                val str = json.asString
                if (str.endsWith("]")) {
                    when (val i = str.substring(str.length - 2, str.length - 1)) {
                        "b" -> NBTTagData(str.substring(0, str.length - 2).split(",").map { NumberConversions.toByte(it) }.toByteArray())
                        "i" -> NBTTagData(str.substring(0, str.length - 2).split(",").map { NumberConversions.toInt(it) }.toIntArray())
                        "l" -> NBTTagData(str.substring(0, str.length - 2).split(",").map { NumberConversions.toLong(it) }.toLongArray())
                        else -> error("unsupported array $json ($i)")
                    }
                } else {
                    when (val i = str.substring(str.length - 1, str.length)) {
                        "b" -> NBTTagData(NumberConversions.toByte(str.substring(0, str.length - 1)))
                        "s" -> NBTTagData(NumberConversions.toShort(str.substring(0, str.length - 1)))
                        "i" -> NBTTagData(NumberConversions.toInt(str.substring(0, str.length - 1)))
                        "l" -> NBTTagData(NumberConversions.toLong(str.substring(0, str.length - 1)))
                        "f" -> NBTTagData(NumberConversions.toFloat(str.substring(0, str.length - 1)))
                        "d" -> NBTTagData(NumberConversions.toDouble(str.substring(0, str.length - 1)))
                        "t" -> NBTTagData(str.substring(0, str.length - 1))
                        else -> error("unsupported type $json ($i)")
                    }
                }
            }
            else -> error("unsupported json $json (${json.javaClass.simpleName})")
        }
    }
}