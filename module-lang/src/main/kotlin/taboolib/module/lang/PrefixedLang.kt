@file:Isolated

package taboolib.module.lang

import taboolib.common.Isolated
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.pluginId
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.colored

fun ProxyCommandSender.sendInfo(node: String, vararg args: Any) {
    sendLeveledLangText(MessageLevel.INFO, node, *args)
}

fun ProxyCommandSender.sendWarn(node: String, vararg args: Any) {
    sendLeveledLangText(MessageLevel.WARN, node, *args)
}

fun ProxyCommandSender.sendError(node: String, vararg args: Any) {
    sendLeveledLangText(MessageLevel.ERROR, node, *args)
}

fun ProxyCommandSender.sendInfoMessage(message: String, vararg args: Any) {
    sendLeveledText(MessageLevel.INFO, message, *args)
}

fun ProxyCommandSender.sendWarnMessage(message: String, vararg args: Any) {
    sendLeveledText(MessageLevel.WARN, message, *args)
}

fun ProxyCommandSender.sendErrorMessage(message: String, vararg args: Any) {
    sendLeveledText(MessageLevel.ERROR, message, *args)
}

fun ProxyCommandSender.sendLeveledLangText(level: MessageLevel, node: String, vararg args: Any) {
    sendMessage(asLeveledLangText(level, node, *args))
}

fun ProxyCommandSender.asLeveledLangText(level: MessageLevel, node: String, vararg args: Any): String {
    return asLeveledText(level, asLangText(node, *args))
}

fun ProxyCommandSender.sendLeveledText(level: MessageLevel, message: String, vararg args: Any) {
    sendMessage(asLeveledText(level, message, *args))
}

fun ProxyCommandSender.asLeveledText(level: MessageLevel, message: String, vararg args: Any): String {
    val prefix = asLangTextOrNull("Prefix-${level.name}") ?: asLangTextOrNull("Prefix") ?: "[$pluginId]"
    return "$prefix &r${message.replaceWithOrder(*args)}".colored()
}

enum class MessageLevel {
    INFO, WARN, ERROR;
}