@file:Isolated

package taboolib.module.configuration.util

import taboolib.common.Isolated
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.colored

/**
 * 获取文本并上色
 *
 * @param node 路径
 * @return 上色后的文本
 */
fun ConfigurationSection.getStringColored(node: String): String? {
    return kotlin.runCatching { getString(node)?.colored() }.getOrElse { error("missing chat module (install(\"module-chat\"))") }
}

/**
 * 获取文本列表并上色
 *
 * @param node 路径
 * @return 上色后的文本
 */
fun ConfigurationSection.getStringListColored(node: String): List<String> {
    return getStringList(node).map { it.colored() }
}