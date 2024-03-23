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

    /**
     * 文件
     */
    var file: File?

    /**
     * 保存为字符串
     */
    fun saveToString(): String

    /**
     * 保存到文件
     *
     * @param file 文件
     */
    fun saveToFile(file: File? = null)

    /**
     * 从文件加载
     *
     * @param file 文件
     */
    fun loadFromFile(file: File)

    /**
     * 从字符串加载
     *
     * @param contents 字符串
     */
    fun loadFromString(contents: String)

    /**
     * 从 [Reader] 加载
     *
     * @param reader 输入流
     */
    fun loadFromReader(reader: Reader)

    /**
     * 从 [InputStream] 加载
     *
     * @param inputStream 输入流
     */
    fun loadFromInputStream(inputStream: InputStream)

    /**
     * 重载
     */
    fun reload()

    /**
     * 注册重载回调
     * 
     * @param runnable 回调
     */
    fun onReload(runnable: Runnable)

    /**
     * 变更类型
     * 
     * @param type 类型
     */
    fun changeType(type: Type)

    companion object {

        /**
         * 创建空配置
         * 
         * @param type 类型
         * @param concurrent 是否支持并发
         * @return [Configuration]
         */
        fun empty(type: Type = Type.YAML, concurrent: Boolean = true): Configuration {
            return ConfigFile(if (concurrent) type.newFormat().createConcurrentConfig() else type.newFormat().createConfig { LinkedHashMap() })
        }

        /**
         * 从文件加载
         * 
         * @param file 文件
         * @param type 类型
         * @param concurrent 是否支持并发
         * @return [Configuration]
         */
        fun loadFromFile(file: File, type: Type? = null, concurrent: Boolean = true): Configuration {
            val format = (type ?: getTypeFromFile(file)).newFormat()
            val configFile = ConfigFile(if (concurrent) format.createConcurrentConfig() else format.createConfig { LinkedHashMap() })
            configFile.loadFromFile(file)
            return configFile
        }

        /**
         * 从 [Reader] 加载
         * 
         * @param reader Reader
         * @param type 类型
         * @param concurrent 是否支持并发
         * @return [Configuration]
         */
        fun loadFromReader(reader: Reader, type: Type = Type.YAML, concurrent: Boolean = true): Configuration {
            val format = type.newFormat()
            val configFile = ConfigFile(if (concurrent) format.createConcurrentConfig() else format.createConfig { LinkedHashMap() })
            configFile.loadFromReader(reader)
            return configFile
        }

        /**
         * 从字符串加载
         * 
         * @param contents 字符串
         * @param type 类型
         * @param concurrent 是否支持并发
         * @return [Configuration]
         */
        fun loadFromString(contents: String, type: Type = Type.YAML, concurrent: Boolean = true): Configuration {
            val format = type.newFormat()
            val configFile = ConfigFile(if (concurrent) format.createConcurrentConfig() else format.createConfig { LinkedHashMap() })
            configFile.loadFromString(contents)
            return configFile
        }

        /**
         * 从 [InputStream] 加载
         *
         * @param inputStream 输入流
         * @param type 类型
         * @param concurrent 是否支持并发
         * @return [Configuration]
         */
        fun loadFromInputStream(inputStream: InputStream, type: Type = Type.YAML, concurrent: Boolean = true): Configuration {
            val format = type.newFormat()
            val configFile = ConfigFile(if (concurrent) format.createConcurrentConfig() else format.createConfig { LinkedHashMap() })
            configFile.loadFromInputStream(inputStream)
            return configFile
        }

        /**
         * 从另一个含有 "saveToString" 方法的配置文件对象加载
         * 
         * @param otherConfig 对象
         * @param type 类型
         * @param concurrent 是否支持并发
         * @return [Configuration]
         */
        fun loadFromOther(otherConfig: Any, type: Type = Type.YAML, concurrent: Boolean = true): Configuration {
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

        /**
         * 反序列化
         * 
         * @param ignoreConstructor 是否忽略构造函数
         * @return T
         */
        inline fun <reified T> ConfigurationSection.toObject(ignoreConstructor: Boolean = false): T {
            return deserialize(this, ignoreConstructor)
        }

        /**
         * 获取值并反序列化
         * 
         * @param key 键
         * @param ignoreConstructor 是否忽略构造函数
         * @return T
         */
        inline fun <reified T> ConfigurationSection.getObject(key: String, ignoreConstructor: Boolean = false): T {
            return deserialize(getConfigurationSection(key) ?: error("Not a section"), ignoreConstructor)
        }

        /**
         * 获取值并反序列化
         * 
         * @param key 键
         * @param obj 原始对象
         * @param ignoreConstructor 是否忽略构造函数
         * @return T
         */
        fun <T> ConfigurationSection.getObject(key: String, obj: T, ignoreConstructor: Boolean = false): T {
            return deserialize(getConfigurationSection(key) ?: error("Not a section"), obj, ignoreConstructor)
        }

        /**
         * 序列化并写入配置文件
         * 
         * @param key 键
         * @param obj 对象
         */
        fun ConfigurationSection.setObject(key: String, obj: Any) {
            set(key, serialize(obj, type))
        }

        /**
         * 序列化
         * 
         * @param obj 对象
         * @param type 类型
         * @param concurrent 是否支持并发
         * @return [ConfigurationSection]
         */
        fun serialize(obj: Any, type: Type = Type.YAML, concurrent: Boolean = true): ConfigurationSection {
            val format = type.newFormat()
            val config = if (concurrent) format.createConcurrentConfig() else format.createConfig { LinkedHashMap() }
            ObjectConverter().toConfig(obj, config)
            return ConfigSection(config)
        }

        /**
         * 反序列化
         * 
         * @param section [ConfigurationSection]
         * @param ignoreConstructor 是否忽略构造函数
         * @return T
         */
        inline fun <reified T> deserialize(section: ConfigurationSection, ignoreConstructor: Boolean = false): T {
            val instance = if (ignoreConstructor) T::class.java.unsafeInstance() as T else T::class.java.invokeConstructor()
            ObjectConverter(ignoreConstructor).toObject((section as ConfigSection).root, instance)
            return instance
        }

        /**
         * 反序列化
         *
         * @param section [ConfigurationSection]
         * @param obj 原始对象
         * @param ignoreConstructor 是否忽略构造函数
         * @return T
         */
        fun <T> deserialize(section: ConfigurationSection, obj: T, ignoreConstructor: Boolean = false): T {
            ObjectConverter(ignoreConstructor).toObject((section as ConfigSection).root, obj)
            return obj
        }

        /**
         * 从 Map 加载 [ConfigurationSection]
         *
         * @param map [Map]
         * @param type 类型
         * @param concurrent 是否支持并发
         * @return [ConfigurationSection]
         */
        fun fromMap(map: Map<*, *>, type: Type = Type.YAML, concurrent: Boolean = true): ConfigurationSection {
            val empty = empty(type, concurrent)
            map.forEach { (k, v) -> empty[k.toString()] = v }
            return empty
        }

        /**
         * 从文件获取类型
         *
         * @param file 文件
         * @param def 默认类型
         * @return [Type]
         */
        fun getTypeFromFile(file: File, def: Type = Type.YAML): Type {
            return getTypeFromExtension(file.extension, def)
        }

        /**
         * 从文件扩展名获取类型
         *
         * @param extension 扩展名
         * @param def 默认类型
         * @return [Type]
         */
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