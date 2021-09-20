@file:Isolated

package taboolib.platform.util

import org.bukkit.command.CommandSender
import taboolib.common.Isolated
import taboolib.common.platform.function.adaptCommandSender
import taboolib.module.lang.*

fun CommandSender.sendInfo(node: String, vararg args: Any) {
    sendLang(Level.INFO, node, *args)
}

fun CommandSender.sendWarn(node: String, vararg args: Any) {
    sendLang(Level.WARN, node, *args)
}

fun CommandSender.sendError(node: String, vararg args: Any) {
    sendLang(Level.ERROR, node, *args)
}

fun CommandSender.sendLang(level: Level, node: String, vararg args: Any) {
    adaptCommandSender(this).sendLang(level, node, *args)
}

fun CommandSender.sendInfoMessage(message: String, vararg args: Any) {
    sendMessage(Level.INFO, message, *args)
}

fun CommandSender.sendWarnMessage(message: String, vararg args: Any) {
    sendMessage(Level.WARN, message, *args)
}

fun CommandSender.sendErrorMessage(message: String, vararg args: Any) {
    sendMessage(Level.ERROR, message, *args)
}

fun CommandSender.sendMessage(level: Level, message: String, vararg args: Any) {
    adaptCommandSender(this).sendMessage(level, message, *args)
}

fun CommandSender.asLangText(level: Level, node: String, vararg args: Any): String {
    return adaptCommandSender(this).asLangText(level, node, *args)
}

fun CommandSender.asLangTextList(level: Level, node: String, vararg args: Any): List<String> {
    return adaptCommandSender(this).asLangTextList(level, node, *args)
}