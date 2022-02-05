package taboolib.internal

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.UnmodifiableConfig
import com.electronwill.nightconfig.core.io.ConfigWriter
import com.electronwill.nightconfig.core.io.WritingException
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.Writer

/**
 * @author 坏黑
 */
@Internal
class YamlWriter : ConfigWriter {

    private val blackConfig = "{}\n"
    private val yamlOptions = DumperOptions()
    private val yamlRepresenter = YamlRepresenter()
    private val yaml = Yaml(yamlRepresenter, yamlOptions)

    override fun write(config: UnmodifiableConfig, writer: Writer) {
        try {
            yamlOptions.indent = 2
            yamlOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            yamlRepresenter.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            var dump = yaml.dump(ConfigSection(config as Config).toMap())
            if (dump == blackConfig) {
                dump = ""
            }
            writer.write(dump)
        } catch (e: Exception) {
            throw WritingException("YAML writing failed", e)
        }
    }
}