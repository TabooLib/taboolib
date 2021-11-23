package taboolib.module.configuration

import com.amihaiemil.eoyaml.Yaml
import com.amihaiemil.eoyaml.YamlNode
import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.UnmodifiableConfig
import com.electronwill.nightconfig.core.io.ConfigWriter
import com.electronwill.nightconfig.core.io.WritingException
import java.io.Writer

/**
 * @author 坏黑
 */
class YamlWriter : ConfigWriter {

    override fun write(config: UnmodifiableConfig, writer: Writer) {
        try {
            Yaml.createYamlPrinter(writer).print(conversion(ConfigSection(config as Config)))
        } catch (e: Exception) {
            throw WritingException("YAML writing failed", e)
        }
    }

    fun conversion(value: Any?, comment: String? = null): YamlNode {
        return when (value) {
            is ConfigSection -> {
                var map = Yaml.createYamlMappingBuilder()
                value.getKeys(false).forEach { k -> map = map.add(k, conversion(value[k], (value.root as CommentedConfig).getComment(k))) }
                map.build(comment.orEmpty())
            }
            is Config -> {
                var map = Yaml.createYamlMappingBuilder()
                value.valueMap().forEach { (k, v) -> map = map.add(k, conversion(v, (value as CommentedConfig).getComment(k))) }
                map.build(comment.orEmpty())
            }
            is Map<*, *> -> {
                var map = Yaml.createYamlMappingBuilder()
                value.forEach { (k, v) -> map = map.add(k.toString(), conversion(v)) }
                map.build(comment.orEmpty())
            }
            is Collection<*> -> {
                var map = Yaml.createYamlSequenceBuilder()
                value.map { map = map.add(conversion(it)) }
                map.build(comment.orEmpty())
            }
            else -> Yaml.createYamlScalarBuilder().addLine(value.toString()).buildPlainScalar(comment.orEmpty(), "")
        }
    }
}