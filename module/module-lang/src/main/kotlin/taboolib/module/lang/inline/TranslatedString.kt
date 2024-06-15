package taboolib.module.lang.inline

import taboolib.common5.eqic
import taboolib.module.lang.Language
import taboolib.module.lang.TypeText

/**
 * Codex
 * taboolib.module.lang.inline.TranslatedString
 *
 * @author 坏黑
 * @since 2024/6/15 20:47
 */
class TranslatedString(node: String, default: String) : Translated<String>(node, default) {

    override fun get(locale: String): String {
        val type = Language.languageCodeTransfer[locale] ?: locale
        val file = Language.languageFile.entries.firstOrNull { it.key.eqic(type) }?.value
            ?: Language.languageFile[Language.default]
            ?: Language.languageFile.values.firstOrNull()
            ?: return default
        val node = file.nodes[node] as? TypeText ?: return default
        return node.text ?: default
    }
}