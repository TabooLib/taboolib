@file:Isolated

package taboolib.platform.util

import org.bukkit.command.CommandSender
import taboolib.common.Isolated
import taboolib.common.platform.function.adaptCommandSender
import taboolib.module.lang.asLangText
import taboolib.module.lang.asLangTextList
import taboolib.module.lang.asLangTextOrNull
import taboolib.module.lang.sendLang

fun CommandSender.sendLang(node: String, vararg args: Any) {
    adaptCommandSender(this).sendLang(node, *args)
}

fun CommandSender.sendLang(node: String, func: (String?) -> String?) {
    adaptCommandSender(this).sendLang(node, func)
}

fun CommandSender.asLangTextOrNull(node: String, vararg args: Any): String? {
    return adaptCommandSender(this).asLangTextOrNull(node, *args)
}

fun CommandSender.asLangTextOrNull(node: String, func: (String?) -> String?): String? {
    return adaptCommandSender(this).asLangTextOrNull(node, func)
}

fun CommandSender.asLangText(node: String, vararg args: Any): String {
    return adaptCommandSender(this).asLangText(node, *args)
}

fun CommandSender.asLangText(node: String, func: (String?) -> String?): String {
    return adaptCommandSender(this).asLangText(node, func)
}

fun CommandSender.asLangTextList(node: String, vararg args: Any): List<String> {
    return adaptCommandSender(this).asLangTextList(node, *args)
}

fun CommandSender.asLangTextList(node: String, func: (String?) -> String?): List<String> {
    return adaptCommandSender(this).asLangTextList(node, func)
}