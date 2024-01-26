package taboolib.module.configuration.util

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.SimpleComponent
import taboolib.module.chat.component

/**
 * 获取 [SimpleComponent]
 *
 * @param node 路径
 * @return [SimpleComponent]
 */
fun ConfigurationSection.getComponent(node: String): SimpleComponent? {
    return runCatching { getString(node)?.component() }.getOrElse { error("missing chat module (install(\"module-chat\"))") }
}

/**
 * 获取 [SimpleComponent]
 *
 * @param node 路径
 * @return [SimpleComponent]
 */
fun ConfigurationSection.getComponentToRaw(node: String): String? {
    return runCatching { getString(node)?.component()?.buildToRaw() }.getOrElse { error("missing chat module (install(\"module-chat\"))") }
}