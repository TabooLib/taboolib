@file:Isolated

package taboolib.platform.util

import org.bukkit.command.CommandSender
import taboolib.common.Isolated
import taboolib.common.platform.function.adaptCommandSender
import taboolib.module.lang.*

fun CommandSender.sendInfo(node: String, vararg args: Any) {
    adaptCommandSender(this).sendInfo(node, *args)
}

fun CommandSender.sendWarn(node: String, vararg args: Any) {
    adaptCommandSender(this).sendWarn(node, *args)
}

fun CommandSender.sendError(node: String, vararg args: Any) {
    adaptCommandSender(this).sendError(node, *args)
}

fun CommandSender.sendInfoMessage(message: String, vararg args: Any) {
    adaptCommandSender(this).sendInfoMessage(message, *args)
}

fun CommandSender.sendWarnMessage(message: String, vararg args: Any) {
    adaptCommandSender(this).sendWarnMessage(message, *args)
}

fun CommandSender.sendErrorMessage(message: String, vararg args: Any) {
    adaptCommandSender(this).sendErrorMessage(message, *args)
}

fun CommandSender.sendLeveledLangText(level: MessageLevel, node: String, vararg args: Any) {
    adaptCommandSender(this).sendLeveledLangText(level, node, *args)
}

fun CommandSender.asLeveledLangTextOrNull(level: MessageLevel, node: String, vararg args: Any): String? {
    val sender = adaptCommandSender(this)
    val message = sender.asLangTextOrNull(node, *args) ?: return null
    return sender.asLeveledText(level, message)
}

fun CommandSender.asLeveledLangText(level: MessageLevel, node: String, vararg args: Any): String {
    return adaptCommandSender(this).asLeveledLangText(level, node, *args)
}

fun CommandSender.sendLeveledText(level: MessageLevel, message: String, vararg args: Any) {
    adaptCommandSender(this).sendLeveledText(level, message, *args)
}

fun CommandSender.asLeveledText(level: MessageLevel, message: String, vararg args: Any): String {
    return adaptCommandSender(this).asLeveledText(level, message, *args)
}