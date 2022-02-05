package taboolib.library.configuration

import taboolib.module.configuration.Type

/**
 * @author Bukkit, 坏黑
 */
interface ConfigurationSection {

    val parent: ConfigurationSection?

    val name: String

    val type: Type

    fun getKeys(deep: Boolean): Set<String>

    operator fun contains(path: String): Boolean

    operator fun get(path: String): Any?

    operator fun get(path: String, def: Any?): Any?

    operator fun set(path: String, value: Any?)

    fun getString(path: String): String?

    fun getString(path: String, def: String?): String?

    fun isString(path: String): Boolean

    fun getInt(path: String): Int

    fun getInt(path: String, def: Int): Int

    fun isInt(path: String): Boolean

    fun getBoolean(path: String): Boolean

    fun getBoolean(path: String, def: Boolean): Boolean

    fun isBoolean(path: String): Boolean

    fun getDouble(path: String): Double

    fun getDouble(path: String, def: Double): Double

    fun isDouble(path: String): Boolean

    fun getLong(path: String): Long

    fun getLong(path: String, def: Long): Long

    fun isLong(path: String): Boolean

    fun getList(path: String): List<*>?

    fun getList(path: String, def: List<*>?): List<*>?

    fun isList(path: String): Boolean

    fun getStringList(path: String): List<String>

    fun getIntegerList(path: String): List<Int>

    fun getBooleanList(path: String): List<Boolean>

    fun getDoubleList(path: String): List<Double>

    fun getFloatList(path: String): List<Float>

    fun getLongList(path: String): List<Long>

    fun getByteList(path: String): List<Byte>

    fun getCharacterList(path: String): List<Char>

    fun getShortList(path: String): List<Short>

    fun getMapList(path: String): List<Map<*, *>>

    fun getConfigurationSection(path: String): ConfigurationSection?

    fun isConfigurationSection(path: String): Boolean

    fun <T : Enum<T>> getEnum(path: String, type: Class<T>): T?

    fun <T : Enum<T>> getEnumList(path: String, type: Class<T>): List<T>

    fun createSection(path: String): ConfigurationSection

    fun toMap(): Map<String, Any?>

    fun getComment(path: String): String?

    fun setComment(path: String, comment: String?)

    fun getValues(deep: Boolean): Map<String, Any?>
}