package taboolib.module.configuration

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

        fun empty(type: Type = Type.YAML): ConfigFile {
            return ConfigFile(type.newFormat().createConfig())
        }

        fun loadFromFile(file: File, type: Type? = null): ConfigFile {
            val configFile = ConfigFile((type ?: getTypeFromFile(file)).newFormat().createConfig())
            configFile.loadFromFile(file)
            return configFile
        }

        fun loadFromReader(reader: Reader, type: Type = Type.YAML): ConfigFile {
            val configFile = ConfigFile(type.newFormat().createConfig())
            configFile.loadFromReader(reader)
            return configFile
        }

        fun loadFromString(contents: String, type: Type = Type.YAML): ConfigFile {
            val configFile = ConfigFile(type.newFormat().createConfig())
            configFile.loadFromString(contents)
            return configFile
        }

        fun loadFromInputStream(inputStream: InputStream, type: Type = Type.YAML): ConfigFile {
            val configFile = ConfigFile(type.newFormat().createConfig())
            configFile.loadFromInputStream(inputStream)
            return configFile
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