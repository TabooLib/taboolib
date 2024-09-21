package taboolib.module.lang.inline

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.util.getStringColored
import taboolib.module.configuration.util.getStringListColored

/**
 * 内嵌语言文件，配合 Configuration 使用。
 * ```yaml
 * lodya_leather_2
 *   lore:
 *     # @lang:lodya_leather_2_name
 *     display: 洛迪亚外套
 *     # @lang:lodya_leather_2_lore
 *     description: |-
 *       城中常见的装扮，
 *       可以在商店中购买。
 * ```
 * 通过注释的形式内嵌翻译键，同时定义默认值。
 *
 * ```kotlin
 * val display = section.getTranslatedString("lore.display")?.get(player)
 * ```
 */
fun ConfigurationSection.getTranslatedString(path: String): TranslatedString? {
    val node = getLanguageNode(path) ?: return null
    val defaultValue = getStringColored(path) ?: return null
    return TranslatedString(node, defaultValue)
}

fun ConfigurationSection.getTranslatedStringList(path: String): TranslatedStringList? {
    val node = getLanguageNode(path) ?: return null
    val defaultValue = if (isList(path)) getStringListColored(path) else getStringColored(path)?.lines() ?: emptyList()
    return TranslatedStringList(node, defaultValue)
}

fun ConfigurationSection.getLanguageNode(path: String): String? {
    val comments = getComments(path)
    for (comment in comments) {
        val line = comment.trim()
        if (line.startsWith("@lang:")) {
            return line.substring(6)
        }
    }
    return null
}