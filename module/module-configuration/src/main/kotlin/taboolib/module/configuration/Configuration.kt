package taboolib.module.configuration

import com.electronwill.nightconfig.core.conversion.ObjectConverter
import org.tabooproject.reflex.Reflex.Companion.invokeConstructor
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.unsafeInstance
import taboolib.library.configuration.ConfigurationSection
import java.io.File
import java.io.InputStream
import java.io.Reader

/**
 * TabooLib
 * taboolib.module.configuration.Configuration
 *
 * @author mac
 * @since 2021/11/22 12:30 上午
 */
interface Configuration : ConfigurationSection {

    var file: File?

    fun saveToString(): String

    fun saveToFile(file: File? = null)

    fun loadFromFile(file: File)

    fun loadFromString(contents: String)

    fun loadFromReader(reader: Reader)

    fun loadFromInputStream(inputStream: InputStream)

    fun reload()

    fun onReload(runnable: Runnable)

    fun changeType(type: Type)

    companion object {

        fun empty(type: Type = Type.YAML, concurrent: Boolean = true): ConfigFile {
            return ConfigFile(if (concurrent) type.newFormat().createConcurrentConfig() else type.newFormat().createConfig { LinkedHashMap() })
        }

        fun loadFromFile(file: File, type: Type? = null, concurrent: Boolean = true): ConfigFile {
            val format = (type ?: getTypeFromFile(file)).newFormat()
            val configFile = ConfigFile(if (concurrent) format.createConcurrentConfig() else format.createConfig { LinkedHashMap() })
            configFile.loadFromFile(file)
            return configFile
        }

        fun loadFromReader(reader: Reader, type: Type = Type.YAML, concurrent: Boolean = true): ConfigFile {
            val format = type.newFormat()
            val configFile = ConfigFile(if (concurrent) format.createConcurrentConfig() else format.createConfig { LinkedHashMap() })
            configFile.loadFromReader(reader)
            return configFile
        }

        fun loadFromString(contents: String, type: Type = Type.YAML, concurrent: Boolean = true): ConfigFile {
            val format = type.newFormat()
            val configFile = ConfigFile(if (concurrent) format.createConcurrentConfig() else format.createConfig { LinkedHashMap() })
            configFile.loadFromString(contents)
            return configFile
        }

        fun loadFromInputStream(inputStream: InputStream, type: Type = Type.YAML, concurrent: Boolean = true): ConfigFile {
            val format = type.newFormat()
            val configFile = ConfigFile(if (concurrent) format.createConcurrentConfig() else format.createConfig { LinkedHashMap() })
            configFile.loadFromInputStream(inputStream)
            return configFile
        }

        fun loadFromOther(otherConfig: Any, type: Type = Type.YAML, concurrent: Boolean = true): ConfigFile {
            return try {
                loadFromString(otherConfig.invokeMethod<String>("saveToString")!!, type, concurrent)
            } catch (ex: NoSuchMethodException) {
                try {
                    loadFromString(otherConfig.toString(), type, concurrent)
                } catch (ex: Throwable) {
                    throw IllegalStateException("Could not load configuration from other configuration", ex)
                }
            }
        }

        inline fun <reified T> Configuration.toObject(ignoreConstructor: Boolean = false): T {
            return deserialize(this, ignoreConstructor)
        }

        inline fun <reified T> ConfigurationSection.getObject(key: String, ignoreConstructor: Boolean = false): T {
            return deserialize(getConfigurationSection(key) ?: error("Not a section"), ignoreConstructor)
        }

        fun <T> ConfigurationSection.getObject(key: String, obj: T, ignoreConstructor: Boolean = false): T {
            return deserialize(getConfigurationSection(key) ?: error("Not a section"), obj, ignoreConstructor)
        }

        fun ConfigurationSection.setObject(key: String, obj: Any) {
            set(key, serialize(obj, type))
        }

        fun serialize(obj: Any, type: Type = Type.YAML, concurrent: Boolean = true): ConfigurationSection {
            val format = type.newFormat()
            val config = if (concurrent) format.createConcurrentConfig() else format.createConfig { LinkedHashMap() }
            ObjectConverter().toConfig(obj, config)
            return ConfigSection(config)
        }

        inline fun <reified T> deserialize(section: ConfigurationSection, ignoreConstructor: Boolean = false): T {
            val instance = if (ignoreConstructor) T::class.java.unsafeInstance() as T else T::class.java.invokeConstructor()
            ObjectConverter(ignoreConstructor).toObject((section as ConfigSection).root, instance)
            return instance
        }

        fun <T> deserialize(section: ConfigurationSection, obj: T, ignoreConstructor: Boolean = false): T {
            ObjectConverter(ignoreConstructor).toObject((section as ConfigSection).root, obj)
            return obj
        }

        fun fromMap(map: Map<*, *>, type: Type = Type.YAML, concurrent: Boolean = true): ConfigurationSection {
            val empty = empty(type, concurrent)
            map.forEach { (k, v) -> empty[k.toString()] = v }
            return empty
        }

        fun getTypeFromFile(file: File, def: Type = Type.YAML): Type {
            return getTypeFromExtension(file.extension, def)
        }

        fun getTypeFromExtension(extension: String, def: Type = Type.YAML): Type {
            return when (extension) {
                "yaml", "yml" -> Type.YAML
                "toml", "tml" -> Type.TOML
                "json" -> Type.JSON
                "conf" -> Type.HOCON
                else -> def
            }
        }
    }
}