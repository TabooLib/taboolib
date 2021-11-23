package taboolib.module.configuration

import com.amihaiemil.eoyaml.Yaml
import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.ConfigFormat
import com.electronwill.nightconfig.core.io.ConfigParser
import com.electronwill.nightconfig.core.io.ParsingException
import com.electronwill.nightconfig.core.io.ParsingMode
import java.io.Reader

/**
 * @author 坏黑
 */
class YamlParser(val configFormat: ConfigFormat<CommentedConfig>) : ConfigParser<CommentedConfig> {

    override fun getFormat(): ConfigFormat<CommentedConfig> {
        return configFormat
    }

    override fun parse(reader: Reader): CommentedConfig {
        val config = configFormat.createConfig()
        parse(reader, config, ParsingMode.MERGE)
        return config
    }

    override fun parse(reader: Reader, destination: Config, parsingMode: ParsingMode) {
        try {
            val mapping = Yaml.createYamlInput(reader.readAll(), true).readYamlMapping()
            val map = yamlMapToMap(mapping)
            map.forEach { (k, v) -> ConfigSection(destination)[k] = v }
        } catch (e: Exception) {
            throw ParsingException("YAML parsing failed", e)
        }
    }
}