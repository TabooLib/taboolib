@file:Isolated

package taboolib.module.lang

import taboolib.common.Isolated
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.pluginId
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.colored

fun ProxyCommandSender.sendInfo(node: String, vararg args: Any) {
    sendLang(Level.INFO, node, *args)
}

fun ProxyCommandSender.sendWarn(node: String, vararg args: Any) {
    sendLang(Level.WARN, node, *args)
}

fun ProxyCommandSender.sendError(node: String, vararg args: Any) {
    sendLang(Level.ERROR, node, *args)
}

fun ProxyCommandSender.sendLang(level: Level, node: String, vararg args: Any) {
    asLangTextList(level, node, *args).forEach { sendMessage(it) }
}

fun ProxyCommandSender.sendInfoMessage(message: String, vararg args: Any) {
    sendMessage(Level.INFO, message, *args)
}

fun ProxyCommandSender.sendWarnMessage(message: String, vararg args: Any) {
    sendMessage(Level.WARN, message, *args)
}

fun ProxyCommandSender.sendErrorMessage(message: String, vararg args: Any) {
    sendMessage(Level.ERROR, message, *args)
}

fun ProxyCommandSender.sendMessage(level: Level, message: String, vararg args: Any) {
    sendMessage(asQualifyText(level, message, *args))
}

fun ProxyCommandSender.asLangText(level: Level, node: String, vararg args: Any): String {
    return asQualifyText(level, asLangText(node, *args))
}

fun ProxyCommandSender.asLangTextList(level: Level, node: String, vararg args: Any): List<String> {
    return asLangTextList(node, *args).map { asQualifyText(level, it, *args) }
}

fun ProxyCommandSender.asQualifyText(level: Level, message: String, vararg args: Any): String {
    val prefix = asLangTextOrNull("prefix-${level.name.lowercase()}") ?: asLangTextOrNull("prefix") ?: "§c[$pluginId]"
    return "$prefix§r $message".replaceWithOrder(*args).colored()
}

@Isolated
enum class Level {

    INFO, WARN, ERROR;
}