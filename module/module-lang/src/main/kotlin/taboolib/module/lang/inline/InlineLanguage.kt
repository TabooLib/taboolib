package taboolib.module.lang.inline

import taboolib.library.configuration.ConfigurationSection

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
    return null
}