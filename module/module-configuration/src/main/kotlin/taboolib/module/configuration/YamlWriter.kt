package taboolib.module.configuration

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.UnmodifiableConfig
import com.electronwill.nightconfig.core.io.ConfigWriter
import com.electronwill.nightconfig.core.io.WritingException
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import taboolib.library.configuration.BukkitYaml
import taboolib.library.configuration.YamlConstructor
import taboolib.library.configuration.YamlRepresenter
import java.io.StringWriter
import java.io.Writer

/**
 * @author 坏黑
 */
class YamlWriter : ConfigWriter {

    private val blackConfig = "{}\n"
    private val dumperOptions = DumperOptions()
    private val loaderOptions = LoaderOptions()
    private val representer: YamlRepresenter
    private val constructor: YamlConstructor
    private val yaml: Yaml
    private val yamlCommentLoader: YamlCommentLoader

    init {
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        loaderOptions.maxAliasesForCollections = Integer.MAX_VALUE
        representer = YamlRepresenter(dumperOptions)
        representer.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        constructor = YamlConstructor(loaderOptions)
        yaml = BukkitYaml(constructor, representer, dumperOptions, loaderOptions)
        yamlCommentLoader = YamlCommentLoader(dumperOptions, loaderOptions, constructor, representer, yaml)
    }

    override fun write(config: UnmodifiableConfig, writer: Writer) {
        if (config !is Config) {
            throw WritingException("YAML writing failed: config cannot be the primitive type")
        }
        try {
            dumperOptions.indent = 2
            dumperOptions.isProcessComments = true
            val node = yamlCommentLoader.toNodeTree(ConfigSection(config))
            val stringWriter = StringWriter()
            if (node.value.isEmpty()) {
                stringWriter.write("")
            } else {
                if (node.value.isEmpty()) {
                    node.flowStyle = DumperOptions.FlowStyle.FLOW
                }
                yaml.serialize(node, stringWriter)
            }
            writer.write(stringWriter.toString())
        } catch (e: Exception) {
            throw WritingException("YAML writing failed", e)
        }
    }
}