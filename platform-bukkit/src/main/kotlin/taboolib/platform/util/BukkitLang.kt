@file:Isolated

package taboolib.platform.util

import org.bukkit.command.CommandSender
import taboolib.common.Isolated
import taboolib.common.platform.adaptCommandSender
import taboolib.module.lang.asLangText
import taboolib.module.lang.asLangTextList
import taboolib.module.lang.sendLang

fun CommandSender.sendLang(node: String, vararg args: Any) {
    adaptCommandSender(this).sendLang(node, *args)
}

fun CommandSender.asLangText(node: String, vararg args: Any): String? {
    return adaptCommandSender(this).asLangText(node, null, *args)
}

fun CommandSender.asLangTextList(node: String, vararg args: Any): List<String> {
    return adaptCommandSender(this).asLangTextList(node, *args)
}