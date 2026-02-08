package taboolib.module.nms

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.isAir
import java.util.*

fun ItemStack?.ifAir(): ItemStack? {
    if (this == null) {
        return null
    }
    if (this.isAir) {
        return null
    }
    if (this.type == Material.AIR) {
        return null
    }
    return this
}

/**
 *  更简单的使用ItemTag
 *
 *  注: 如果修改了ItemTag 需要使用 write 设置回去
 */
fun ItemTag.reader(reader: ItemTagReader.() -> Unit) {
    val itemTagReader = ItemTagReader(this)
    reader.invoke(itemTagReader)
}

fun ItemStack?.itemTagReader(reader: ItemTagReader.() -> Unit) {
    val item = this.ifAir() ?: return
    item.getItemTag().reader(reader)
}

data class ItemTagReader(var itemTag: ItemTag) {

    /**
     * 获取到子节点的所有key
     * parent: 父节点 默认为main
     */
    fun getKeys(parent: String = "main"): Set<String> {
        if (parent == "main") {
            return itemTag.keys
        }
        return itemTag.getDeep(parent)?.asCompound()?.keys ?: setOf()
    }

    fun toJson(): String {
        return itemTag.toJson()
    }

    fun formatJson(): String {
        return itemTag.toJsonFormatted()
    }

    fun loadFormJson(json: String) {
        itemTag = ItemTag.fromJson(json)
    }

    fun getString(key: String, def: String): String {
        return itemTag.getDeepOrElse(key, ItemTagData(def)).asString()
    }

    fun getString(key: String): String? {
        return itemTag.getDeep(key)?.asString()
    }

    fun getInt(key: String, def: Int): Int {
        return itemTag.getDeepOrElse(key, ItemTagData(def)).asInt()
    }

    fun getInt(key: String): Int? {
        return itemTag.getDeep(key)?.asInt()
    }

    fun getDouble(key: String, def: Double): Double {
        return itemTag.getDeepOrElse(key, ItemTagData(def)).asDouble()
    }

    fun getDouble(key: String): Double? {
        return itemTag.getDeep(key)?.asDouble()
    }

    fun getStringList(key: String): List<String> {
        return itemTag.getDeep(key)?.asList()?.map { it.asString() } ?: listOf()
    }

    fun getDoubleList(key: String): List<Double> {
        return itemTag.getDeep(key)?.asList()?.map { it.asDouble() } ?: listOf()
    }

    fun getBoolean(key: String, def: Boolean): Boolean {
        return getString(key, def.toString()).toBoolean()
    }

    fun getBoolean(key: String): Boolean {
        return getString(key)?.toBoolean() ?: false
    }

    fun getLong(key: String, def: Long): Long {
        return itemTag.getDeepOrElse(key, ItemTagData(def)).asLong()
    }

    fun getLong(key: String): Long? {
        return itemTag.getDeep(key)?.asLong()
    }

    fun getFloat(key: String, def: Float): Float {
        return itemTag.getDeepOrElse(key, ItemTagData(def)).asFloat()
    }

    fun getFloat(key: String): Float? {
        return itemTag.getDeep(key)?.asFloat()
    }

    fun getByte(key: String, def: Byte): Byte {
        return itemTag.getDeepOrElse(key, ItemTagData(def)).asByte()
    }

    fun getByte(key: String): Byte? {
        return itemTag.getDeep(key)?.asByte()
    }

    fun getUUID(key: String): UUID? {
        return itemTag.getDeep(key)?.asString()?.let { UUID.fromString(it) }
    }

    fun getUUID(key: String, def: UUID): UUID {
        return itemTag.getDeepOrElse(key, ItemTagData(def.toString())).asString().let { UUID.fromString(it) }
    }

    fun write(itemStack: ItemStack) {
        itemTag.saveTo(itemStack)
    }

    fun saveToItem(itemStack: ItemStack) {
        itemTag.saveTo(itemStack)
    }

    fun putAll(map: Map<String, Any?>) {
        itemTag.putAll(map.mapNotNull { it.key to ItemTagData.toNBT(it.value) })
    }

    operator fun set(key: String, value: Any?) {
        if (value == null) {
            itemTag.removeDeep(key)
        } else {
            if (value is UUID || value is Boolean) {
                itemTag.putDeep(key, value.toString())
                return
            }
            itemTag.putDeep(key, value)
        }
    }

    fun remove(key: String) {
        itemTag.removeDeep(key)
    }

    fun close(): ItemTag {
        return itemTag
    }

    fun containsKey(key: String): Boolean {
        return itemTag.containsKey(key)
    }

    fun containsValue(value: ItemTagData): Boolean {
        return itemTag.containsValue(value)
    }
}
