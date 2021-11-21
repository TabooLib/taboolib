package taboolib.module.configuration.toml

import com.moandjiezana.toml.Toml
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common5.Coerce
import taboolib.library.configuration.ConfigurationDefault
import taboolib.library.configuration.ConfigurationSection

/**
 * TabooLib
 * taboolib.module.configuration.toml.TomlSection
 *
 * @author mac
 * @since 2021/11/21 11:00 下午
 */
open class TomlSection(
    val root: Toml = Toml(),
    private val parent: ConfigurationSection? = null,
    private val path: String = "",
    private val fullPath: String = createPath(parent, path),
) : ConfigurationSection {

    protected val lock = Any()

    var separator = '.'
    val values: MutableMap<String, Any>
        get() = root.getProperty<MutableMap<String, Any>>("values")!!

    override fun getKeys(deep: Boolean): MutableSet<String> {
        val result = LinkedHashSet<String>()
        mapChildrenKeys(result, this, deep)
        return result
    }

    override fun getValues(deep: Boolean): MutableMap<String, Any?> {
        val result = LinkedHashMap<String, Any?>()
        mapChildrenValues(result, this, deep)
        return result
    }

    override fun contains(path: String): Boolean {
        return get(path) != null
    }

    override fun isSet(path: String): Boolean {
        return contains(path)
    }

    override fun getCurrentPath(): String {
        return fullPath
    }

    override fun getName(): String {
        return path
    }

    override fun getParent(): ConfigurationSection? {
        return parent
    }

    override fun get(path: String): Any? {
        return get(path, null)
    }

    override fun get(path: String, def: Any?): Any? {
        if (path.isEmpty()) {
            return this
        }
        var i1 = -1
        var i2: Int
        var section: ConfigurationSection? = this
        while (path.indexOf(separator, (i1 + 1).also { i2 = it }).also { i1 = it } != -1) {
            section = section!!.getConfigurationSection(path.substring(i2, i1))
            if (section == null) {
                return def
            }
        }
        val key = path.substring(i2)
        if (section === this) {
            return values[key] ?: def
        }
        return section!![key, def]
    }

    override fun set(path: String, value: Any?) {
        synchronized(lock) {
            var i1 = -1
            var i2: Int
            var section: ConfigurationSection = this
            while (path.indexOf(separator, (i1 + 1).also { i2 = it }).also { i1 = it } != -1) {
                val node = path.substring(i2, i1)
                val subSection = section.getConfigurationSection(node)
                section = subSection ?: section.createSection(node)
            }
            val key = path.substring(i2)
            if (section === this) {
                if (value == null) {
                    values.remove(key)
                } else {
                    values[key] = value
                }
            } else {
                section[key] = value
            }
        }
    }

    override fun createSection(path: String): ConfigurationSection {
        var i1 = -1
        var i2: Int
        var section: ConfigurationSection = this
        while (path.indexOf(separator, (i1 + 1).also { i2 = it }).also { i1 = it } != -1) {
            val node = path.substring(i2, i1)
            val subSection = section.getConfigurationSection(node)
            section = subSection ?: section.createSection(node)
        }
        val key = path.substring(i2)
        if (section === this) {
            val result = TomlSection(Toml(), this, key)
            values[key] = result.values
            return result
        }
        return section.createSection(key)
    }

    override fun createSection(path: String, map: MutableMap<*, *>): ConfigurationSection {
        val section = createSection(path)
        map.forEach { (key, value) ->
            if (value is Map<*, *>) {
                section.createSection(key.toString(), value)
            } else {
                section[key.toString()] = value
            }
        }
        return section
    }

    override fun getString(path: String): String? {
        return get(path) as? String
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

    override fun getList(path: String): MutableList<*>? {
        return get(path) as? MutableList<*>
    }

    override fun getList(path: String, def: MutableList<*>?): MutableList<*>? {
        return get(path) as? MutableList<*> ?: def
    }

    override fun isList(path: String): Boolean {
        return get(path) is List<*>
    }

    override fun getStringList(path: String): MutableList<String> {
        return getList(path)?.map { it.toString() }?.toMutableList() ?: ArrayList()
    }

    override fun getIntegerList(path: String): MutableList<Int> {
        return getList(path)?.map { Coerce.toInteger(it) }?.toMutableList() ?: ArrayList()
    }

    override fun getBooleanList(path: String): MutableList<Boolean> {
        return getList(path)?.map { Coerce.toBoolean(it) }?.toMutableList() ?: ArrayList()
    }

    override fun getDoubleList(path: String): MutableList<Double> {
        return getList(path)?.map { Coerce.toDouble(it) }?.toMutableList() ?: ArrayList()
    }

    override fun getFloatList(path: String): MutableList<Float> {
        return getList(path)?.map { Coerce.toFloat(it) }?.toMutableList() ?: ArrayList()
    }

    override fun getLongList(path: String): MutableList<Long> {
        return getList(path)?.map { Coerce.toLong(it) }?.toMutableList() ?: ArrayList()
    }

    override fun getByteList(path: String): MutableList<Byte> {
        return getList(path)?.map { Coerce.toByte(it) }?.toMutableList() ?: ArrayList()
    }

    override fun getCharacterList(path: String): MutableList<Char> {
        return getList(path)?.map { Coerce.toChar(it) }?.toMutableList() ?: ArrayList()
    }

    override fun getShortList(path: String): MutableList<Short> {
        return getList(path)?.map { Coerce.toShort(it) }?.toMutableList() ?: ArrayList()
    }

    override fun getMapList(path: String): MutableList<MutableMap<*, *>> {
        return getList(path)?.filterIsInstance<Map<*, *>>()?.map { it.toMutableMap() }?.toMutableList() ?: ArrayList()
    }

    override fun getConfigurationSection(path: String): ConfigurationSection? {
        var value = get(path, null)
        if (value != null) {
            return if (value is ConfigurationSection) (value as ConfigurationSection?)!! else null
        }
        value = get(path)
        return if (value is ConfigurationSection) createSection(path) else null
    }

    override fun isConfigurationSection(path: String): Boolean {
        return get(path) is ConfigurationSection
    }

    protected fun mapChildrenKeys(output: MutableSet<String>, section: ConfigurationSection, deep: Boolean) {
        if (section is TomlSection) {
            section.values.forEach { (key, value) ->
                output.add(createPath(section, key))
                if (value is ConfigurationSection && deep) {
                    mapChildrenKeys(output, value, deep)
                }
            }
        } else {
            section.getKeys(deep).mapTo(output) { createPath(section, it) }
        }
    }

    protected fun mapChildrenValues(output: MutableMap<String, Any?>, section: ConfigurationSection, deep: Boolean) {
        if (section is TomlSection) {
            section.values.forEach { (key, value) ->
                output[createPath(section, key)] = value
                if (value is ConfigurationSection && deep) {
                    mapChildrenValues(output, value, deep)
                }
            }
        } else {
            section.getValues(deep).forEach { (key, value) -> output[createPath(section, key)] = value }
        }
    }

    override fun getDefaultSection(): ConfigurationSection? {
        TODO("Not supported")
    }

    override fun addDefault(path: String, value: Any?) {
        TODO("Not supported")
    }

    override fun getRoot(): ConfigurationDefault? {
        TODO("Not supported")
    }

    companion object {

        fun createPath(section: ConfigurationSection?, key: String?, separator: Char = '.'): String {
            val builder = StringBuilder()
            var parent: ConfigurationSection? = section
            while (parent != null && parent != section) {
                if (builder.isNotEmpty()) {
                    builder.insert(0, separator)
                }
                builder.insert(0, parent.name)
                parent = parent.parent
            }
            if (key != null && key.isNotEmpty()) {
                if (builder.isNotEmpty()) {
                    builder.append(separator)
                }
                builder.append(key)
            }
            return builder.toString()
        }
    }
}