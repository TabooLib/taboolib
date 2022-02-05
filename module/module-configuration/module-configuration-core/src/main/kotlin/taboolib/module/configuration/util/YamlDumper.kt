package taboolib.module.configuration.util

import taboolib.common.Isolated
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type

@Isolated
object YamlDumper {

    fun dumpAll(key: String, value: Any?, space: Int = 2): String {
        return if (key.contains('.')) {
            "${key.substringBefore('.')}:\n${" ".repeat(space)}${dumpAll(key.substringAfter('.'), value, space + 2)}"
        } else {
            val dump = dump(value)
            when {
                dump.startsWith("-") -> "$key:\n$dump"
                value is List<*> && value.isEmpty() -> "$key: []"
                value is Map<*, *> -> if (value.isEmpty()) "$key: {}" else "$key:\n$dump"
                value is ConfigurationSection -> "$key:\n  ${dump.replace("\n", "\n  ")}"
                else -> "$key: $dump"
            }
        }
    }

    fun dump(data: Any?): String {
        if (data == null) {
            return ""
        }
        var single = false
        val dump = Configuration.empty(Type.YAML)
        when (data) {
            is ConfigurationSection -> {
                data.getValues(false).forEach { (path, value) -> dump[path] = value }
            }
            is Map<*, *> -> {
                data.forEach { (k, v) -> dump[k.toString()] = v }
            }
            else -> {
                single = true
                dump["value"] = data
            }
        }
        val save = if (single) {
            dump.saveToString().substring("value:".length).trim().split('\n').toTypedArray()
        } else {
            dump.saveToString().trim().split('\n').toTypedArray()
        }
        return java.lang.String.join("\n", *save)
    }
}