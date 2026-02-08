package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer

/**
 * 向命令发送者发送语言节点对应的消息
 *
 * @param node 语言节点
 * @param args 参数
 */
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

/**
 * 获取语言节点对应的文本
 *
 * @param node 语言节点
 * @param args 参数
 * @return 文本内容，若节点不存在则返回 "{$node}"
 */
fun ProxyCommandSender.asLangText(node: String, vararg args: Any): String {
    return asLangTextOrNull(node, *args) ?: "{$node}"
}

/**
 * 获取语言节点对应的文本
 *
 * @param node 语言节点
 * @param args 参数
 * @return 文本内容，若节点不存在则返回 null
 */
fun ProxyCommandSender.asLangTextOrNull(node: String, vararg args: Any): String? {
    val file = getLocaleFile()
    if (file != null) {
        return (file.nodes[node] as? TypeText)?.asText(this, *args)
    }
    return null
}

/**
 * 获取语言节点对应的文本列表
 *
 * @param node 语言节点
 * @param args 参数
 * @return 文本列表
 */
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

/**
 * 获取命令发送者的语言设置
 *
 * @return 语言代码
 */
fun ProxyCommandSender.getLocale(): String {
    return if (this is ProxyPlayer) Language.getLocale(this) else Language.getLocale()
}

/**
 * 获取命令发送者对应的语言文件
 *
 * @return 语言文件，若不存在则返回默认语言文件或第一个可用的语言文件
 */
fun ProxyCommandSender.getLocaleFile(): LanguageFile? {
    val locale = getLocale()
    return Language.languageFile.entries.firstOrNull { it.key.equals(locale, true) }?.value
        ?: Language.languageFile[Language.default]
        ?: Language.languageFile.values.firstOrNull()
}

/**
 * 注册语言
 *
 * @param code 语言代码
 */
fun registerLanguage(vararg code: String) {
    Language.addLanguage(*code)
}