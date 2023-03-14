package taboolib.module.configuration

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.EnumGetMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.util.decodeUnicode
import taboolib.common5.Coerce
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.util.Commented
import taboolib.module.configuration.util.CommentedList

/**
 * TabooLib
 * taboolib.module.configuration.TomlSection
 *
 * @author mac
 * @since 2021/11/21 11:00 下午
 */
open class ConfigSection(var root: Config, override val name: String = "", override val parent: ConfigurationSection? = null) : ConfigurationSection {

    private val configType = Type.getType(root.configFormat())

    override val type: Type
        get() = configType

    override fun getKeys(deep: Boolean): Set<String> {
        val keys = LinkedHashSet<String>()
        fun process(map: Map<String, Any?>, parent: String = "") {
            map.forEach { (k, v) ->
                if (v is Config) {
                    if (deep) {
                        process(v.valueMap(), "$parent$k.")
                    } else {
                        keys += "$parent$k"
                    }
                } else {
                    keys += "$parent$k"
                }
            }
        }
        process(root.valueMap())
        return keys
    }

    override fun contains(path: String): Boolean {
        return root.contains(path)
    }

    override fun get(path: String): Any? {
        return get(path, null)
    }

    override fun get(path: String, def: Any?): Any? {
        // 不知道为什么大家都喜欢用 getConfigurationSection("")
        // 感觉都是一个师傅教的
        if (path.isEmpty()) {
            return this
        }
        var name = path
        var parent: ConfigurationSection? = null
        if (path.contains('.')) {
            name = path.substringAfterLast('.')
            parent = getConfigurationSection(path.substringBeforeLast('.').substringAfterLast('.'))
        }
        return when (val value = root.getOrElse(path, def)) {
            is Config -> ConfigSection(value, name, parent)
            // 理论是无法获取到 Map 类型
            // 因为在 set 方法中 Map 会被转换为 Config 类型
            is Map<*, *> -> {
                val subConfig = root.createSubConfig()
                subConfig.setProperty("map", value)
                ConfigSection(subConfig, name, parent)
            }
            else -> unwrap(value)
        }
    }

    override fun set(path: String, value: Any?) {
        when {
            value == null -> root.remove(path)
            value is List<*> -> root.set<Any>(path, unwrap(value, this))
            value is Collection<*> && value !is List<*> -> set(path, value.toList())
            value is ConfigurationSection -> set(path, value.getConfig())
            value is Map<*, *> -> set(path, value.toConfig(this))
            value is Commented -> {
                set(path, value.value)
                setComment(path, value.comment)
            }
            value is CommentedList -> {
                set(path, value.value)
                setComments(path, value.comment)
            }
            else -> root.set<Any>(path, value)
        }
    }

    override fun getString(path: String): String? {
        val value = get(path) ?: return null
        return if (value is List<*>) value.joinToString("\n") else value.toString()
    }

    override fun getString(path: String, def: String?): String? {
        return getString(path) ?: def
    }

    override fun isString(path: String): Boolean {
        return get(path) is String
    }

    override fun getInt(path: String): Int {
        return Coerce.toInteger(get(path))
    }

    override fun getInt(path: String, def: Int): Int {
        return Coerce.toInteger(get(path) ?: def)
    }

    override fun isInt(path: String): Boolean {
        val value = get(path)
        return value is Long || value is Int
    }

    override fun getBoolean(path: String): Boolean {
        return Coerce.toBoolean(get(path))
    }

    override fun getBoolean(path: String, def: Boolean): Boolean {
        return Coerce.toBoolean(get(path) ?: def)
    }

    override fun isBoolean(path: String): Boolean {
        return get(path) is Double
    }

    override fun getDouble(path: String): Double {
        return Coerce.toDouble(get(path))
    }

    override fun getDouble(path: String, def: Double): Double {
        return Coerce.toDouble(get(path) ?: def)
    }

    override fun isDouble(path: String): Boolean {
        return get(path) is Double
    }

    override fun getLong(path: String): Long {
        return Coerce.toLong(get(path))
    }

    override fun getLong(path: String, def: Long): Long {
        return Coerce.toLong(get(path) ?: def)
    }

    override fun isLong(path: String): Boolean {
        return get(path) is Long
    }

    override fun getList(path: String): List<*>? {
        return (get(path) as? List<*>)?.map { unwrap(it) }?.toList()
    }

    override fun getList(path: String, def: List<*>?): List<*>? {
        return get(path) as? List<*> ?: def
    }

    override fun isList(path: String): Boolean {
        return get(path) is List<*>
    }

    override fun getStringList(path: String): List<String> {
        return getList(path)?.map { it.toString() }?.toList() ?: ArrayList()
    }

    override fun getIntegerList(path: String): List<Int> {
        return getList(path)?.map { Coerce.toInteger(it) }?.toList() ?: ArrayList()
    }

    override fun getBooleanList(path: String): List<Boolean> {
        return getList(path)?.map { Coerce.toBoolean(it) }?.toList() ?: ArrayList()
    }

    override fun getDoubleList(path: String): List<Double> {
        return getList(path)?.map { Coerce.toDouble(it) }?.toList() ?: ArrayList()
    }

    override fun getFloatList(path: String): List<Float> {
        return getList(path)?.map { Coerce.toFloat(it) }?.toList() ?: ArrayList()
    }

    override fun getLongList(path: String): List<Long> {
        return getList(path)?.map { Coerce.toLong(it) }?.toList() ?: ArrayList()
    }

    override fun getByteList(path: String): List<Byte> {
        return getList(path)?.map { Coerce.toByte(it) }?.toList() ?: ArrayList()
    }

    override fun getCharacterList(path: String): List<Char> {
        return getList(path)?.map { Coerce.toChar(it) }?.toList() ?: ArrayList()
    }

    override fun getShortList(path: String): List<Short> {
        return getList(path)?.map { Coerce.toShort(it) }?.toList() ?: ArrayList()
    }

    override fun getMapList(path: String): List<Map<*, *>> {
        return getList(path)?.filterIsInstance<Map<*, *>>()?.toList() ?: ArrayList()
    }

    override fun getConfigurationSection(path: String): ConfigurationSection? {
        return get(path) as? ConfigurationSection
    }

    override fun isConfigurationSection(path: String): Boolean {
        return get(path) is ConfigurationSection
    }

    override fun <T : Enum<T>> getEnum(path: String, type: Class<T>): T? {
        return root.getEnum(path, type)
    }

    override fun <T : Enum<T>> getEnumList(path: String, type: Class<T>): List<T> {
        return getStringList(path).mapNotNull { EnumGetMethod.NAME_IGNORECASE.get(it, type) }
    }

    override fun createSection(path: String): ConfigurationSection {
        val subConfig = root.createSubConfig()
        set(path, subConfig)
        var name = path
        var parent: ConfigurationSection? = null
        if (path.contains('.')) {
            name = path.substringAfterLast('.')
            parent = getConfigurationSection(path.substringBeforeLast('.').substringAfterLast('.'))
        }
        return ConfigSection(subConfig, name, parent)
    }

    override fun toMap(): Map<String, Any?> {
        fun process(map: Map<String, Any?>): Map<String, Any?> {
            val newMap = LinkedHashMap<String, Any?>()
            map.forEach { (k, v) -> newMap[k] = unwrap(v) }
            return newMap
        }
        return process(root.valueMap())
    }

    override fun getComment(path: String): String? {
        return (root as? CommentedConfig)?.getComment(path)
    }

    override fun getComments(path: String): List<String> {
        return getComment(path)?.lines() ?: emptyList()
    }

    override fun setComment(path: String, comment: String?) {
        (root as? CommentedConfig)?.setComment(path, if (comment?.isBlank() == true) null else comment)
    }

    override fun setComments(path: String, comments: List<String>) {
        return setComment(path, comments.joinToString("\n"))
    }

    override fun addComments(path: String, comments: List<String>) {
        getComments(path).toMutableList().apply {
            addAll(comments)
            setComments(path, this)
        }
    }

    override fun getValues(deep: Boolean): Map<String, Any?> {
        return getKeys(deep).associateWith { get(it) }
    }

    override fun toString(): String {
        return root.configFormat().createWriter().writeToString(root)
    }

    override fun clear() {
        root.clear()
    }

    companion object {

        private fun ConfigurationSection.getConfig(): Config {
            return if (this is ConfigSection) root else error("Not supported")
        }

        private fun Map<*, *>.toConfig(parent: ConfigSection): Config {
            val section = ConfigSection(parent.root.createSubConfig())
            forEach { (k, v) -> section[k.toString()] = v }
            return section.root
        }

        fun unwrap(v: Any?): Any? {
            return when (v) {
                "~", "null" -> null
                "''", "\"\"" -> ""
                else -> when (v) {
                    is ConfigSection -> v.toMap()
                    is ConfigurationSection -> unwrap(v.getConfig())
                    is Config -> unwrap(v.valueMap())
                    is Collection<*> -> v.map { unwrap(it) }.toList()
                    is Map<*, *> -> v.map { it.key to unwrap(it.value) }.toMap()
                    is String -> v.decodeUnicode()
                    else -> v
                }
            }
        }

        fun unwrap(list: List<*>, parent: ConfigSection): List<*> {
            fun process(value: Any?): Any? {
                return when {
                    value is List<*> -> unwrap(value, parent)
                    value is Collection<*> && value !is List<*> -> value.toList()
                    value is ConfigurationSection -> value.getConfig()
                    value is Map<*, *> -> value.toConfig(parent)
                    else -> value
                }
            }
            return list.map { process(it) }
        }
    }
}