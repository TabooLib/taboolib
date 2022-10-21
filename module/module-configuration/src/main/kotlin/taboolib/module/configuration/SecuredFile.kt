package taboolib.module.configuration

import taboolib.library.configuration.ConfigurationSection
import java.io.File

@Suppress("DEPRECATION")
@Deprecated("Use Configuration")
class SecuredFile : ConfigFile(Type.YAML.newFormat().createConcurrentConfig()) {

    companion object {

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
            val dump = SecuredFile()
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

        fun loadConfiguration(contents: String): SecuredFile {
            val config = SecuredFile()
            try {
                config.loadFromString(contents)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return config
        }

        fun loadConfiguration(file: File): SecuredFile {
            val config = SecuredFile()
            try {
                config.loadFromFile(file)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return config
        }
    }
}