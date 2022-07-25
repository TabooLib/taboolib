@file:Isolated

package taboolib.platform.util

import org.bukkit.command.CommandSender
import taboolib.common.Isolated
import taboolib.common.platform.function.adaptCommandSender
import taboolib.module.lang.*

fun CommandSender.sendInfo(node: String, vararg args: Any) {
    sendLang(Level.INFO, node, *args)
}

fun CommandSender.sendInfo(node: String, func: (String?) -> String?) {
    sendLang(Level.INFO, node, func)
}

fun CommandSender.sendWarn(node: String, vararg args: Any) {
    sendLang(Level.WARN, node, *args)
}

fun CommandSender.sendWarn(node: String, func: (String?) -> String?) {
    sendLang(Level.WARN, node, func)
}

fun CommandSender.sendError(node: String, vararg args: Any) {
    sendLang(Level.ERROR, node, *args)
}

fun CommandSender.sendError(node: String, func: (String?) -> String?) {
    sendLang(Level.ERROR, node, func)
}

fun CommandSender.sendLang(level: Level, node: String, vararg args: Any) {
    adaptCommandSender(this).sendLang(level, node, *args)
}

fun CommandSender.sendLang(level: Level, node: String, func: (String?) -> String?) {
    adaptCommandSender(this).sendLang(level, node, func)
}

fun CommandSender.sendInfoMessage(message: String, vararg args: Any) {
    sendMessage(Level.INFO, message, *args)
}

fun CommandSender.sendInfoMessage(message: String, func: (String?) -> String?) {
    sendMessage(Level.INFO, message, func)
}

fun CommandSender.sendWarnMessage(message: String, vararg args: Any) {
    sendMessage(Level.WARN, message, *args)
}

fun CommandSender.sendWarnMessage(message: String, func: (String?) -> String?) {
    sendMessage(Level.WARN, message, func)
}

fun CommandSender.sendErrorMessage(message: String, vararg args: Any) {
    sendMessage(Level.ERROR, message, *args)
}

fun CommandSender.sendErrorMessage(message: String, func: (String?) -> String?) {
    sendMessage(Level.ERROR, message, func)
}

fun CommandSender.sendMessage(level: Level, message: String, vararg args: Any) {
    adaptCommandSender(this).sendMessage(level, message, *args)
}

fun CommandSender.sendMessage(level: Level, message: String, func: (String?) -> String?) {
    adaptCommandSender(this).sendMessage(level, message, func)
}

fun CommandSender.asLangText(level: Level, node: String, vararg args: Any): String {
    return adaptCommandSender(this).asLangText(level, node, *args)
}

fun CommandSender.asLangText(level: Level, node: String, func: (String?) -> String?): String {
    return adaptCommandSender(this).asLangText(level, node, func)
}

fun CommandSender.asLangTextList(level: Level, node: String, vararg args: Any): List<String> {
    return adaptCommandSender(this).asLangTextList(level, node, *args)
}

fun CommandSender.asLangTextList(level: Level, node: String, func: (String?) -> String?): List<String> {
    return adaptCommandSender(this).asLangTextList(level, node, func)
}