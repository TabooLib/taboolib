package taboolib.module.configuration

import com.amihaiemil.eoyaml.Node
import com.amihaiemil.eoyaml.Yaml
import com.amihaiemil.eoyaml.YamlMapping
import com.amihaiemil.eoyaml.YamlNode
import com.electronwill.nightconfig.core.Config
import java.io.Reader

class CommentValue(val value: Any, val comment: String)

internal fun Reader.readAll(): String {
    return readText().replace("\t", "  ")
}

internal fun yamlMapToMap(map: YamlMapping): Map<String, CommentValue> {
    return map.keys().associate { it.asScalar().value() to yamlNodeToValue(map.value(it)) }
}

internal fun yamlNodeToValue(node: YamlNode): CommentValue {
    return when (node.type()) {
        Node.SCALAR -> CommentValue(node.asScalar().value(), node.asScalar().comment().value())
        Node.MAPPING -> CommentValue(yamlMapToMap(node.asMapping()), node.asMapping().comment().value())
        Node.SEQUENCE -> CommentValue(node.asSequence().values().map { yamlNodeToValue(it) }.toList(), node.asSequence().comment().value())
        else -> error("Not supported")
    }
}

internal fun mapToYamlNode(sourceMap: Map<*, *>): YamlNode {
    var map = Yaml.createYamlMappingBuilder()
    sourceMap.forEach { (k, v) -> map = map.add(k.toString(), valueToYamlNode(v ?: return@forEach)) }
    return map.build()
}

internal fun valueToYamlNode(value: Any): YamlNode {
    return when (value) {
        is Map<*, *> -> mapToYamlNode(value)
        is List<*> -> {
            var seq = Yaml.createYamlSequenceBuilder()
            value.forEach { seq = seq.add(valueToYamlNode(it!!)) }
            seq.build()
        }
        is Config -> mapToYamlNode(value.valueMap())
        else -> Yaml.createYamlScalarBuilder().addLine(value.toString()).buildPlainScalar()
    }
}