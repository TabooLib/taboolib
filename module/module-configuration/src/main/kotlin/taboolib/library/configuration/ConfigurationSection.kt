package taboolib.library.configuration

import taboolib.module.configuration.Type

/**
 * @author Bukkit, 坏黑
 */
interface ConfigurationSection {

    val parent: ConfigurationSection?

    val name: String

    val type: Type

    /**
     * Gets a set containing all keys in this section.
     *
     *
     * If deep is set to true, then this will contain all the keys within any
     * child [ConfigurationSection]s (and their children, etc). These
     * will be in a valid path notation for you to use.
     *
     *
     * If deep is set to false, then this will contain only the keys of any
     * direct children, and not their own children.
     *
     * @param deep Whether or not to get a deep list, as opposed to a shallow
     * list.
     * @return Set of keys contained within this ConfigurationSection.
     */
    fun getKeys(deep: Boolean): Set<String>

    /**
     * Checks if this [ConfigurationSection] contains the given path.
     *
     *
     * If the value for the requested path does not exist but a default value
     * has been specified, this will return true.
     *
     * @param path Path to check for existence.
     * @return True if this section contains the requested path, either via
     * default or being set.
     * @throws IllegalArgumentException Thrown when path is null.
     */
    operator fun contains(path: String): Boolean

    /**
     * Gets the requested Object by path.
     *
     *
     * If the Object does not exist but a default value has been specified,
     * this will return the default value. If the Object does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the Object to get.
     * @return Requested Object.
     */
    operator fun get(path: String): Any?

    /**
     * Gets the requested Object by path, returning a default value if not
     * found.
     *
     * @param path Path of the Object to get.
     * @param def  The default value to return if the path is not found.
     * @return Requested Object.
     */
    operator fun get(path: String, def: Any?): Any?

    /**
     * Sets the specified path to the given value.
     *
     *
     * If value is null, the entry will be removed. Any existing entry will be
     * replaced, regardless of what the new value is.
     *
     * @param path Path of the object to set.
     * @param value New value to set the path to.
     */
    operator fun set(path: String, value: Any?)

    /**
     * Gets the requested String by path.
     *
     *
     * If the String does not exist but a default value has been specified,
     * this will return the default value. If the String does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the String to get.
     * @return Requested String.
     */
    fun getString(path: String): String?

    /**
     * Gets the requested String by path, returning a default value if not
     * found.
     *
     * @param path Path of the String to get.
     * @param def The default value to return if the path is not found or is
     * not a String.
     * @return Requested String.
     */
    fun getString(path: String, def: String?): String?

    /**
     * Checks if the specified path is a String.
     *
     *
     * If the path exists but is not a String, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a String and return appropriately.
     *
     * @param path Path of the String to check.
     * @return Whether or not the specified path is a String.
     */
    fun isString(path: String): Boolean

    /**
     * Gets the requested int by path.
     *
     *
     * If the int does not exist but a default value has been specified, this
     * will return the default value. If the int does not exist and no default
     * value was specified, this will return 0.
     *
     * @param path Path of the int to get.
     * @return Requested int.
     */
    fun getInt(path: String): Int

    /**
     * Gets the requested int by path, returning a default value if not found.
     *
     * @param path Path of the int to get.
     * @param def The default value to return if the path is not found or is
     * not an int.
     * @return Requested int.
     */
    fun getInt(path: String, def: Int): Int

    /**
     * Checks if the specified path is an int.
     *
     *
     * If the path exists but is not a int, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a int and return appropriately.
     *
     * @param path Path of the int to check.
     * @return Whether or not the specified path is an int.
     */
    fun isInt(path: String): Boolean

    /**
     * Gets the requested boolean by path.
     *
     *
     * If the boolean does not exist but a default value has been specified,
     * this will return the default value. If the boolean does not exist and
     * no default value was specified, this will return false.
     *
     * @param path Path of the boolean to get.
     * @return Requested boolean.
     */
    fun getBoolean(path: String): Boolean

    /**
     * Gets the requested boolean by path, returning a default value if not
     * found.
     *
     * @param path Path of the boolean to get.
     * @param def The default value to return if the path is not found or is
     * not a boolean.
     * @return Requested boolean.
     */
    fun getBoolean(path: String, def: Boolean): Boolean

    /**
     * Checks if the specified path is a boolean.
     *
     *
     * If the path exists but is not a boolean, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a boolean and return appropriately.
     *
     * @param path Path of the boolean to check.
     * @return Whether or not the specified path is a boolean.
     */
    fun isBoolean(path: String): Boolean

    /**
     * Gets the requested double by path.
     *
     *
     * If the double does not exist but a default value has been specified,
     * this will return the default value. If the double does not exist and no
     * default value was specified, this will return 0.
     *
     * @param path Path of the double to get.
     * @return Requested double.
     */
    fun getDouble(path: String): Double

    /**
     * Gets the requested double by path, returning a default value if not
     * found.
     *
     * @param path Path of the double to get.
     * @param def The default value to return if the path is not found or is
     * not a double.
     * @return Requested double.
     */
    fun getDouble(path: String, def: Double): Double

    /**
     * Checks if the specified path is a double.
     *
     *
     * If the path exists but is not a double, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a double and return appropriately.
     *
     * @param path Path of the double to check.
     * @return Whether or not the specified path is a double.
     */
    fun isDouble(path: String): Boolean

    /**
     * Gets the requested long by path.
     *
     *
     * If the long does not exist but a default value has been specified, this
     * will return the default value. If the long does not exist and no
     * default value was specified, this will return 0.
     *
     * @param path Path of the long to get.
     * @return Requested long.
     */
    fun getLong(path: String): Long

    /**
     * Gets the requested long by path, returning a default value if not
     * found.
     *
     * @param path Path of the long to get.
     * @param def The default value to return if the path is not found or is
     * not a long.
     * @return Requested long.
     */
    fun getLong(path: String, def: Long): Long

    /**
     * Checks if the specified path is a long.
     *
     *
     * If the path exists but is not a long, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a long and return appropriately.
     *
     * @param path Path of the long to check.
     * @return Whether or not the specified path is a long.
     */
    fun isLong(path: String): Boolean

    /**
     * Gets the requested List by path.
     *
     *
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the List to get.
     * @return Requested List.
     */
    fun getList(path: String): List<*>?

    /**
     * Gets the requested List by path, returning a default value if not
     * found.
     *
     * @param path Path of the List to get.
     * @param def The default value to return if the path is not found or is
     * not a List.
     * @return Requested List.
     */
    fun getList(path: String, def: List<*>?): List<*>?

    /**
     * Checks if the specified path is a List.
     *
     *
     * If the path exists but is not a List, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a List and return appropriately.
     *
     * @param path Path of the List to check.
     * @return Whether or not the specified path is a List.
     */
    fun isList(path: String): Boolean

    /**
     * Gets the requested List of String by path.
     *
     *
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     *
     *
     * This method will attempt to cast any values into a String if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of String.
     */
    fun getStringList(path: String): List<String>

    /**
     * Gets the requested List of Integer by path.
     *
     *
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     *
     *
     * This method will attempt to cast any values into a Integer if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Integer.
     */
    fun getIntegerList(path: String): List<Int>

    /**
     * Gets the requested List of Boolean by path.
     *
     *
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     *
     *
     * This method will attempt to cast any values into a Boolean if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Boolean.
     */
    fun getBooleanList(path: String): List<Boolean>

    /**
     * Gets the requested List of Double by path.
     *
     *
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     *
     *
     * This method will attempt to cast any values into a Double if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Double.
     */
    fun getDoubleList(path: String): List<Double>

    /**
     * Gets the requested List of Float by path.
     *
     *
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     *
     *
     * This method will attempt to cast any values into a Float if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Float.
     */
    fun getFloatList(path: String): List<Float>

    /**
     * Gets the requested List of Long by path.
     *
     *
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     *
     *
     * This method will attempt to cast any values into a Long if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Long.
     */
    fun getLongList(path: String): List<Long>

    /**
     * Gets the requested List of Byte by path.
     *
     *
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     *
     *
     * This method will attempt to cast any values into a Byte if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Byte.
     */
    fun getByteList(path: String): List<Byte>

    /**
     * Gets the requested List of Character by path.
     *
     *
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     *
     *
     * This method will attempt to cast any values into a Character if
     * possible, but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Character.
     */
    fun getCharacterList(path: String): List<Char>

    /**
     * Gets the requested List of Short by path.
     *
     *
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     *
     *
     * This method will attempt to cast any values into a Short if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Short.
     */
    fun getShortList(path: String): List<Short>

    /**
     * Gets the requested List of Maps by path.
     *
     *
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     *
     *
     * This method will attempt to cast any values into a Map if possible, but
     * may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Maps.
     */
    fun getMapList(path: String): List<Map<*, *>>

    /**
     * Gets the requested ConfigurationSection by path.
     *
     *
     * If the ConfigurationSection does not exist but a default value has been
     * specified, this will return the default value. If the
     * ConfigurationSection does not exist and no default value was specified,
     * this will return null.
     *
     * @param path Path of the ConfigurationSection to get.
     * @return Requested ConfigurationSection.
     */
    fun getConfigurationSection(path: String): ConfigurationSection?

    /**
     * Checks if the specified path is a ConfigurationSection.
     *
     *
     * If the path exists but is not a ConfigurationSection, this will return
     * false. If the path does not exist, this will return false. If the path
     * does not exist but a default value has been specified, this will check
     * if that default value is a ConfigurationSection and return
     * appropriately.
     *
     * @param path Path of the ConfigurationSection to check.
     * @return Whether or not the specified path is a ConfigurationSection.
     */
    fun isConfigurationSection(path: String): Boolean

    fun <T : Enum<T>> getEnum(path: String, type: Class<T>): T?

    fun <T : Enum<T>> getEnumList(path: String, type: Class<T>): List<T>

    fun createSection(path: String): ConfigurationSection

    fun toMap(): Map<String, Any?>

    fun getComment(path: String): String?

    fun getComments(path: String): List<String>

    fun setComment(path: String, comment: String?)

    fun setComments(path: String, comments: List<String>)

    fun addComments(path: String, comments: List<String>)

    fun getValues(deep: Boolean): Map<String, Any?>

    fun clear()
}