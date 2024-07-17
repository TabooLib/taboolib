package taboolib.module.lang.inline

import taboolib.common5.eqic
import taboolib.module.lang.Language
import taboolib.module.lang.TypeList
import taboolib.module.lang.TypeText

/**
 * Codex
 * taboolib.module.lang.inline.TranslatedStringList
 *
 * @author 坏黑
 * @since 2024/6/15 20:47
 */
open class TranslatedStringList(node: String, default: List<String>) : Translated<List<String>>(node, default) {

    override fun get(locale: String): List<String> {
        val type = Language.languageCodeTransfer[locale] ?: locale
        val file = Language.languageFile.entries.firstOrNull { it.key.eqic(type) }?.value
            ?: Language.languageFile[Language.default]
            ?: Language.languageFile.values.firstOrNull()
            ?: return default
        return when (val node = file.nodes[node]) {
            is TypeText -> node.text?.let { listOf(it) } ?: default
            is TypeList -> node.list.filterIsInstance<TypeText>().map { it.text ?: "" }
            else -> default
        }
    }

    companion object {

        fun of(value: List<String>): TranslatedStringList {
            return object : TranslatedStringList("", value) {
                override fun get(locale: String): List<String> {
                    return value
                }
            }
        }
    }
}