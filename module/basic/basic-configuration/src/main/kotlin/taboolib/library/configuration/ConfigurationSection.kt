package taboolib.library.configuration

import taboolib.module.configuration.Type

/**
 * @author Bukkit, 坏黑
 */
interface ConfigurationSection {

    /** 原始配置对象 */
    val primitiveConfig: Any

    /** 父节点 */
    val parent: ConfigurationSection?

    /** 节点名称 */
    var name: String

    /** 节点类型 */
    val type: Type

    /**
     * 获取此节点中所有键的集合。
     *
     * 如果 deep 设置为 true，则将包含任何子 [ConfigurationSection] 中的所有键
     * （以及它们的子键等）。这些键将以有效的路径表示法呈现，以供使用。
     *
     * 如果 deep 设置为 false，则只包含直接子节点的键，而不包括它们自己的子节点。
     *
     * @param deep 是否获取深层列表，而不是浅层列表。
     * @return 包含在此 ConfigurationSection 中的键集合。
     */
    fun getKeys(deep: Boolean): Set<String>

    /**
     * 检查此 [ConfigurationSection] 是否包含给定路径。
     *
     * 如果请求路径的值不存在但已指定默认值，则此方法将返回 true。
     *
     * @param path 要检查存在性的路径。
     * @return 如果此节点包含请求的路径（通过默认值或已设置），则返回 true。
     * @throws IllegalArgumentException 当路径为 null 时抛出。
     */
    operator fun contains(path: String): Boolean

    /**
     * 通过路径获取请求的对象。
     *
     * 如果对象不存在但已指定默认值，则将返回默认值。
     * 如果对象不存在且未指定默认值，则返回 null。
     *
     * @param path 要获取的对象的路径。
     * @return 请求的对象。
     */
    operator fun get(path: String): Any?

    /**
     * 通过路径获取请求的对象，如果未找到则返回默认值。
     *
     * @param path 要获取的对象的路径。
     * @param def 如果未找到路径时要返回的默认值。
     * @return 请求的对象。
     */
    operator fun get(path: String, def: Any?): Any?

    /**
     * 将指定路径设置为给定值。
     *
     * 如果值为 null，则会删除该条目。任何现有条目都将被替换，无论新值是什么。
     *
     * @param path 要设置的对象的路径。
     * @param value 要设置的新值。
     */
    operator fun set(path: String, value: Any?)

    /**
     * 通过路径获取请求的字符串。
     *
     * 如果字符串不存在但已指定默认值，则将返回默认值。
     * 如果字符串不存在且未指定默认值，则返回 null。
     *
     * @param path 要获取的字符串的路径。
     * @return 请求的字符串。
     */
    fun getString(path: String): String?

    /**
     * 通过路径获取请求的字符串，如果未找到则返回默认值。
     *
     * @param path 要获取的字符串的路径。
     * @param def 如果未找到路径或不是字符串时要返回的默认值。
     * @return 请求的字符串。
     */
    fun getString(path: String, def: String?): String?

    /**
     * 检查指定路径是否为字符串。
     *
     * 如果路径存在但不是字符串，则返回 false。
     * 如果路径不存在，则返回 false。
     * 如果路径不存在但已指定默认值，则检查该默认值是否为字符串并相应返回。
     *
     * @param path 要检查的字符串的路径。
     * @return 指定路径是否为字符串。
     */
    fun isString(path: String): Boolean

    /**
     * 通过路径获取请求的整数。
     *
     * 如果整数不存在但已指定默认值，则将返回默认值。
     * 如果整数不存在且未指定默认值，则返回 0。
     *
     * @param path 要获取的整数的路径。
     * @return 请求的整数。
     */
    fun getInt(path: String): Int

    /**
     * 通过路径获取请求的整数，如果未找到则返回默认值。
     *
     * @param path 要获取的整数的路径。
     * @param def 如果未找到路径或不是整数时要返回的默认值。
     * @return 请求的整数。
     */
    fun getInt(path: String, def: Int): Int

    /**
     * 检查指定路径是否为整数。
     *
     * 如果路径存在但不是整数，则返回 false。
     * 如果路径不存在，则返回 false。
     * 如果路径不存在但已指定默认值，则检查该默认值是否为整数并相应返回。
     *
     * @param path 要检查的整数的路径。
     * @return 指定路径是否为整数。
     */
    fun isInt(path: String): Boolean

    /**
     * 通过路径获取请求的布尔值。
     *
     * 如果布尔值不存在但已指定默认值，则将返回默认值。
     * 如果布尔值不存在且未指定默认值，则返回 false。
     *
     * @param path 要获取的布尔值的路径。
     * @return 请求的布尔值。
     */
    fun getBoolean(path: String): Boolean

    /**
     * 通过路径获取请求的布尔值，如果未找到则返回默认值。
     *
     * @param path 要获取的布尔值的路径。
     * @param def 如果未找到路径或不是布尔值时要返回的默认值。
     * @return 请求的布尔值。
     */
    fun getBoolean(path: String, def: Boolean): Boolean

    /**
     * 检查指定路径是否为布尔值。
     *
     * 如果路径存在但不是布尔值，则返回 false。
     * 如果路径不存在，则返回 false。
     * 如果路径不存在但已指定默认值，则检查该默认值是否为布尔值并相应返回。
     *
     * @param path 要检查的布尔值的路径。
     * @return 指定路径是否为布尔值。
     */
    fun isBoolean(path: String): Boolean

    /**
     * 通过路径获取请求的双精度浮点数。
     *
     * 如果双精度浮点数不存在但已指定默认值，则将返回默认值。
     * 如果双精度浮点数不存在且未指定默认值，则返回 0。
     *
     * @param path 要获取的双精度浮点数的路径。
     * @return 请求的双精度浮点数。
     */
    fun getDouble(path: String): Double

    /**
     * 通过路径获取请求的双精度浮点数，如果未找到则返回默认值。
     *
     * @param path 要获取的双精度浮点数的路径。
     * @param def 如果未找到路径或不是双精度浮点数时要返回的默认值。
     * @return 请求的双精度浮点数。
     */
    fun getDouble(path: String, def: Double): Double

    /**
     * 检查指定路径是否为双精度浮点数。
     *
     * 如果路径存在但不是双精度浮点数，则返回 false。
     * 如果路径不存在，则返回 false。
     * 如果路径不存在但已指定默认值，则检查该默认值是否为双精度浮点数并相应返回。
     *
     * @param path 要检查的双精度浮点数的路径。
     * @return 指定路径是否为双精度浮点数。
     */
    fun isDouble(path: String): Boolean

    /**
     * 通过路径获取请求的长整数。
     *
     * 如果长整数不存在但已指定默认值，则将返回默认值。
     * 如果长整数不存在且未指定默认值，则返回 0。
     *
     * @param path 要获取的长整数的路径。
     * @return 请求的长整数。
     */
    fun getLong(path: String): Long

    /**
     * 通过路径获取请求的长整数，如果未找到则返回默认值。
     *
     * @param path 要获取的长整数的路径。
     * @param def 如果未找到路径或不是长整数时要返回的默认值。
     * @return 请求的长整数。
     */
    fun getLong(path: String, def: Long): Long

    /**
     * 检查指定路径是否为长整数。
     *
     * 如果路径存在但不是长整数，则返回 false。
     * 如果路径不存在，则返回 false。
     * 如果路径不存在但已指定默认值，则检查该默认值是否为长整数并相应返回。
     *
     * @param path 要检查的长整数的路径。
     * @return 指定路径是否为长整数。
     */
    fun isLong(path: String): Boolean

    /**
     * 通过路径获取请求的列表。
     *
     * 如果列表不存在但已指定默认值，则将返回默认值。
     * 如果列表不存在且未指定默认值，则返回 null。
     *
     * @param path 要获取的列表的路径。
     * @return 请求的列表。
     */
    fun getList(path: String): List<*>?

    /**
     * 通过路径获取请求的列表，如果未找到则返回默认值。
     *
     * @param path 要获取的列表的路径。
     * @param def 如果未找到路径或不是列表时要返回的默认值。
     * @return 请求的列表。
     */
    fun getList(path: String, def: List<*>?): List<*>?

    /**
     * 检查指定路径是否为列表。
     *
     * 如果路径存在但不是列表，则返回 false。
     * 如果路径不存在，则返回 false。
     * 如果路径不存在但已指定默认值，则检查该默认值是否为列表并相应返回。
     *
     * @param path 要检查的列表的路径。
     * @return 指定路径是否为列表。
     */
    fun isList(path: String): Boolean

    /**
     * 通过路径获取请求的字符串列表。
     *
     * 如果列表不存在但已指定默认值，则将返回默认值。
     * 如果列表不存在且未指定默认值，则返回空列表。
     *
     * 此方法将尝试将任何值转换为字符串（如果可能），但如果不兼容，可能会遗漏一些值。
     *
     * @param path 要获取的列表的路径。
     * @return 请求的字符串列表。
     */
    fun getStringList(path: String): List<String>

    /**
     * 通过路径获取请求的整数列表。
     *
     * 如果列表不存在但已指定默认值，则将返回默认值。
     * 如果列表不存在且未指定默认值，则返回空列表。
     *
     * 此方法将尝试将任何值转换为整数（如果可能），但如果不兼容，可能会遗漏一些值。
     *
     * @param path 要获取的列表的路径。
     * @return 请求的整数列表。
     */
    fun getIntegerList(path: String): List<Int>

    /**
     * 通过路径获取请求的布尔值列表。
     *
     * 如果列表不存在但已指定默认值，则将返回默认值。
     * 如果列表不存在且未指定默认值，则返回空列表。
     *
     * 此方法将尝试将任何值转换为布尔值（如果可能），但如果不兼容，可能会遗漏一些值。
     *
     * @param path 要获取的列表的路径。
     * @return 请求的布尔值列表。
     */
    fun getBooleanList(path: String): List<Boolean>

    /**
     * 通过路径获取请求的双精度浮点数列表。
     *
     * 如果列表不存在但已指定默认值，则将返回默认值。
     * 如果列表不存在且未指定默认值，则返回空列表。
     *
     * 此方法将尝试将任何值转换为双精度浮点数（如果可能），但如果不兼容，可能会遗漏一些值。
     *
     * @param path 要获取的列表的路径。
     * @return 请求的双精度浮点数列表。
     */
    fun getDoubleList(path: String): List<Double>

    /**
     * 通过路径获取请求的浮点数列表。
     *
     * 如果列表不存在但已指定默认值，则将返回默认值。
     * 如果列表不存在且未指定默认值，则返回空列表。
     *
     * 此方法将尝试将任何值转换为浮点数（如果可能），但如果不兼容，可能会遗漏一些值。
     *
     * @param path 要获取的列表的路径。
     * @return 请求的浮点数列表。
     */
    fun getFloatList(path: String): List<Float>

    /**
     * 通过路径获取请求的长整数列表。
     *
     * 如果列表不存在但已指定默认值，则将返回默认值。
     * 如果列表不存在且未指定默认值，则返回空列表。
     *
     * 此方法将尝试将任何值转换为长整数（如果可能），但如果不兼容，可能会遗漏一些值。
     *
     * @param path 要获取的列表的路径。
     * @return 请求的长整数列表。
     */
    fun getLongList(path: String): List<Long>

    /**
     * 通过路径获取请求的字节列表。
     *
     * 如果列表不存在但已指定默认值，则将返回默认值。
     * 如果列表不存在且未指定默认值，则返回空列表。
     *
     * 此方法将尝试将任何值转换为字节（如果可能），但如果不兼容，可能会遗漏一些值。
     *
     * @param path 要获取的列表的路径。
     * @return 请求的字节列表。
     */
    fun getByteList(path: String): List<Byte>

    /**
     * 通过路径获取请求的字符列表。
     *
     * 如果列表不存在但已指定默认值，则将返回默认值。
     * 如果列表不存在且未指定默认值，则返回空列表。
     *
     * 此方法将尝试将任何值转换为字符（如果可能），但如果不兼容，可能会遗漏一些值。
     *
     * @param path 要获取的列表的路径。
     * @return 请求的字符列表。
     */
    fun getCharacterList(path: String): List<Char>

    /**
     * 通过路径获取请求的短整数列表。
     *
     * 如果列表不存在但已指定默认值，则将返回默认值。
     * 如果列表不存在且未指定默认值，则返回空列表。
     *
     * 此方法将尝试将任何值转换为短整数（如果可能），但如果不兼容，可能会遗漏一些值。
     *
     * @param path 要获取的列表的路径。
     * @return 请求的短整数列表。
     */
    fun getShortList(path: String): List<Short>

    /**
     * 通过路径获取请求的映射列表。
     *
     * 如果列表不存在但已指定默认值，则将返回默认值。
     * 如果列表不存在且未指定默认值，则返回空列表。
     *
     * 此方法将尝试将任何值转换为映射（如果可能），但如果不兼容，可能会遗漏一些值。
     *
     * @param path 要获取的列表的路径。
     * @return 请求的映射列表。
     */
    fun getMapList(path: String): List<Map<*, *>>

    /**
     * 通过路径获取请求的 ConfigurationSection。
     *
     * 如果 ConfigurationSection 不存在但已指定默认值，则将返回默认值。
     * 如果 ConfigurationSection 不存在且未指定默认值，则返回 null。
     *
     * @param path 要获取的 ConfigurationSection 的路径。
     * @return 请求的 ConfigurationSection。
     */
    fun getConfigurationSection(path: String): ConfigurationSection?

    /**
     * 检查指定路径是否为 ConfigurationSection。
     *
     * 如果路径存在但不是 ConfigurationSection，则返回 false。
     * 如果路径不存在，则返回 false。
     * 如果路径不存在但已指定默认值，则检查该默认值是否为 ConfigurationSection 并相应返回。
     *
     * @param path 要检查的 ConfigurationSection 的路径。
     * @return 指定路径是否为 ConfigurationSection。
     */
    fun isConfigurationSection(path: String): Boolean

    /**
     * 通过路径获取请求的枚举值。
     *
     * @param path 要获取的枚举值的路径。
     * @param type 枚举类的 Class 对象。
     * @return 请求的枚举值，如果未找到则返回 null。
     */
    fun <T : Enum<T>> getEnum(path: String, type: Class<T>): T?

    /**
     * 通过路径获取请求的枚举值列表。
     *
     * @param path 要获取的枚举值列表的路径。
     * @param type 枚举类的 Class 对象。
     * @return 请求的枚举值列表。
     */
    fun <T : Enum<T>> getEnumList(path: String, type: Class<T>): List<T>

    /**
     * 在指定路径创建一个新的 ConfigurationSection。
     *
     * @param path 要创建的 ConfigurationSection 的路径。
     * @return 新创建的 ConfigurationSection。
     */
    fun createSection(path: String): ConfigurationSection

    /**
     * 将当前 ConfigurationSection 转换为 Map。
     *
     * @return 包含当前 ConfigurationSection 所有键值对的 Map。
     */
    fun toMap(): Map<String, Any?>

    /**
     * 获取指定路径的注释。
     *
     * @param path 要获取注释的路径。
     * @return 指定路径的注释，如果没有注释则返回 null。
     */
    fun getComment(path: String): String?

    /**
     * 获取指定路径的注释列表。
     *
     * @param path 要获取注释的路径。
     * @return 指定路径的注释列表。
     */
    fun getComments(path: String): List<String>

    /**
     * 设置指定路径的注释。
     *
     * @param path 要设置注释的路径。
     * @param comment 要设置的注释，如果为 null 则删除现有注释。
     */
    fun setComment(path: String, comment: String?)

    /**
     * 设置指定路径的注释列表。
     *
     * @param path 要设置注释的路径。
     * @param comments 要设置的注释列表。
     */
    fun setComments(path: String, comments: List<String>)

    /**
     * 向指定路径添加注释。
     *
     * @param path 要添加注释的路径。
     * @param comments 要添加的注释列表。
     */
    fun addComments(path: String, comments: List<String>)

    /**
     * 获取当前 ConfigurationSection 的所有值。
     *
     * @param deep 是否包含子节点的值。
     * @return 包含所有值的 Map。
     */
    fun getValues(deep: Boolean): Map<String, Any?>

    /**
     * 清除当前 ConfigurationSection 中的所有值。
     */
    fun clear()
}