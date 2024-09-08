package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer

fun ProxyCommandSender.sendLang(node: String, vararg args: Any) {
    val file = getLocaleFile()
    if (file == null) {
        sendMessage("{$node}")
    } else {
        val type = file.nodes[node]
        if (type != null) {
            type.send(this, *args)
        } else {
            sendMessage("{$node}")
        }
    }
}

fun ProxyCommandSender.asLangText(node: String, vararg args: Any): String {
    return asLangTextOrNull(node, *args) ?: "{$node}"
}

fun ProxyCommandSender.asLangTextOrNull(node: String, vararg args: Any): String? {
    val file = getLocaleFile()
    if (file != null) {
        return (file.nodes[node] as? TypeText)?.asText(this, *args)
    }
    return null
}

fun ProxyCommandSender.asLangTextList(node: String, vararg args: Any): List<String> {
    val file = getLocaleFile()
    return if (file == null) {
        listOf("{$node}")
    } else {
        when (val type = file.nodes[node]) {
            is TypeText -> {
                val text = type.asText(this, *args)
                if (text != null) listOf(text) else emptyList()
            }
            is TypeList -> {
                type.asTextList(this, *args)
            }
            else -> {
                listOf("{$node}")
            }
        }
    }
}

fun ProxyCommandSender.getLocale(): String {
    return if (this is ProxyPlayer) Language.getLocale(this) else Language.getLocale()
}

fun ProxyCommandSender.getLocaleFile(): LanguageFile? {
    val locale = getLocale()
    return Language.languageFile.entries.firstOrNull { it.key.equals(locale, true) }?.value
        ?: Language.languageFile[Language.default]
        ?: Language.languageFile.values.firstOrNull()
}

fun registerLanguage(vararg code: String) {
    Language.addLanguage(*code)
}