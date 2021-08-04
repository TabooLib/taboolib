package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer

fun ProxyCommandSender.sendLang(node: String, vararg args: Any) {
    val file = getLocaleFile()
    if (file == null) {
        sendMessage("Language file not found")
    } else {
        val type = file.nodes[node]
        if (type != null) {
            type.send(this, *args)
        } else {
            sendMessage("Language node not found: $node")
        }
    }
}

fun ProxyCommandSender.asLangText(node: String, vararg args: Any): String {
    val file = getLocaleFile()
    return if (file == null) {
        "Language file not found"
    } else {
        val type = file.nodes[node]
        if (type is TypeText) {
            type.asText(this, *args) ?: ""
        } else {
            "Language node not found: $node"
        }
    }
}

fun ProxyCommandSender.asLangTextList(node: String, vararg args: Any): List<String> {
    val file = getLocaleFile()
    return if (file == null) {
        listOf("Language file not found")
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
                listOf("Language node not found: $node")
            }
        }
    }
}

fun ProxyCommandSender.getLocale(): String {
    return if (this is ProxyPlayer) Language.getLocale(this) else Language.getLocale()
}

fun ProxyCommandSender.getLocaleFile(): LanguageFile? {
    val locale = getLocale()
    return Language.languageFile.entries.firstOrNull { it.key.equals(locale, true) }?.value ?: Language.languageFile.values.firstOrNull()
}

fun registerLanguage(vararg code: String) {
    Language.addLanguage(*code)
}