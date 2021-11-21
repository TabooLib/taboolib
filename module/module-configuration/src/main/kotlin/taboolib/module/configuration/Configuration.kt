package taboolib.module.configuration

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.toml.TomlFile
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

    companion object {

        fun getFileType(file: File): Type? {
            return when (file.extension) {
                "yaml", "yml" -> Type.YAML
                "toml", "tml" -> Type.TOML
                else -> null
            }
        }

        fun loadFromFile(file: File, frame: Type = Type.YAML): Configuration {
            when (getFileType(file)) {
                Type.YAML -> return SecuredFile.loadConfiguration(file)
                Type.TOML -> return TomlFile().also { it.loadFromFile(file) }
            }
            return when (frame) {
                Type.YAML -> SecuredFile.loadConfiguration(file)
                Type.TOML -> TomlFile().also { it.loadFromFile(file) }
            }
        }

        fun loadFromString(contents: String, frame: Type = Type.YAML): Configuration {
            return when (frame) {
                Type.YAML -> SecuredFile.loadConfiguration(contents)
                Type.TOML -> TomlFile().also { it.loadFromString(contents) }
            }
        }

        fun loadFromReader(reader: Reader, frame: Type = Type.YAML): Configuration {
            return when (frame) {
                Type.YAML -> SecuredFile().also { it.loadFromReader(reader) }
                Type.TOML -> TomlFile().also { it.loadFromReader(reader) }
            }
        }

        fun loadFromInputStream(inputStream: InputStream, frame: Type = Type.YAML): Configuration {
            return when (frame) {
                Type.YAML -> SecuredFile().also { it.loadFromInputStream(inputStream) }
                Type.TOML -> TomlFile().also { it.loadFromInputStream(inputStream) }
            }
        }
    }
}