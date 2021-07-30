package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer

fun registerLanguage(vararg code: String) {
    Language.languageCode.addAll(code)
    Language.reload()
}

fun ProxyCommandSender.sendLang(node: String, vararg args: Any) {
    val languageFile = Language.languageFile[getLocale()] ?: Language.languageFile[Language.languageCode.firstOrNull() ?: "zh_CN"]
    if (languageFile == null) {
        sendMessage("Language file not found")
    } else {
        val type = languageFile.nodes[node]
        if (type != null) {
            type.send(this, *args)
        } else {
            sendMessage("Language node not found: $node")
        }
    }
}

fun ProxyCommandSender.asLangText(node: String, def: String? = null, vararg args: Any): String {
    val languageFile = Language.languageFile[getLocale()] ?: Language.languageFile[Language.languageCode.firstOrNull() ?: "zh_CN"]
    return if (languageFile == null) {
        "Language file not found"
    } else {
        val type = languageFile.nodes[node]
        if (type is TypeText) {
            type.asText(this, def, *args).toString()
        } else {
            "Language node not found: $node"
        }
    }
}

fun ProxyCommandSender.asLangTextList(node: String, vararg args: Any): List<String> {
    val languageFile = Language.languageFile[getLocale()] ?: Language.languageFile[Language.languageCode.firstOrNull() ?: "zh_CN"]
    return if (languageFile == null) {
        listOf("Language file not found")
    } else {
        val type = languageFile.nodes[node]
        if (type is TypeText) {
            type.asTextList(this, *args)
        } else {
            listOf("Language node not found: $node")
        }
    }
}

fun ProxyCommandSender.getLocale(): String {
    return if (this is ProxyPlayer) Language.getLocale(this) else Language.getLocale()
}