@file:Isolated

package taboolib.module.configuration.util

import taboolib.common.Isolated
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.colored

fun ConfigurationSection.getStringColored(node: String): String? {
    return kotlin.runCatching { getString(node)?.colored() }.getOrElse { error("missing chat module (install(\"module-chat\"))") }
}

fun ConfigurationSection.getStringListColored(node: String): List<String> {
    return getStringList(node).map { it.colored() }
}