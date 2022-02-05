package taboolib.internal

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.ConfigFormat
import com.electronwill.nightconfig.core.io.ConfigParser
import com.electronwill.nightconfig.core.io.ParsingException
import com.electronwill.nightconfig.core.io.ParsingMode
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import taboolib.library.configuration.ConfigurationSection
import java.io.Reader

/**
 * @author 坏黑
 */
@Internal
class YamlParser(val configFormat: ConfigFormat<Config>) : ConfigParser<Config> {

    private val yamlOptions = DumperOptions()
    private val yamlRepresenter = YamlRepresenter()
    private val yaml = Yaml(yamlRepresenter, yamlOptions)

    override fun getFormat(): ConfigFormat<Config> {
        return configFormat
    }

    override fun parse(reader: Reader): Config {
        val config = configFormat.createConfig()
        parse(reader, config, ParsingMode.MERGE)
        return config
    }

    override fun parse(reader: Reader, destination: Config, parsingMode: ParsingMode) {
        try {
            loadFromString(reader.readText(), ConfigSection(destination))
        } catch (e: Exception) {
            throw ParsingException("YAML parsing failed", e)
        }
    }

    fun loadFromString(contents: String, section: ConfigurationSection) {
        convert(yaml.load(contents) ?: return, section)
    }

    fun convert(input: Map<*, *>, section: ConfigurationSection) {
        input.forEach { (k, value) ->
            val key = k.toString()
            if (value is Map<*, *>) {
                convert(value, section.createSection(key))
            } else {
                section[key] = value
            }
        }
    }
}