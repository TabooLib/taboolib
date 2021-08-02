package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.console
import taboolib.common.platform.pluginId
import taboolib.module.chat.colored
import java.text.MessageFormat

/**
 * 原作者 @author Polar-Pumpkin
 * 由 Mical 移动构建部分方法
 * @date 2021/8/1 23:10:39
 */

fun ProxyCommandSender.buildMessage(level: LevelType, message: String, vararg args: Any): String {
    val file = getLocaleFile()
    if (file == null) {
        return "Language file not found"
    } else {
        val prefix = file.nodes["Level-Prefix"]
        if (prefix != null) {
            when (level) {
                LevelType.INFO, LevelType.WARN, LevelType.ERROR -> {
                    val info = file.nodes[level.node()]
                    return if (info != null) {
                        (asLangText("Level-Prefix") + asLangText(level.node()) + MessageFormat.format(
                            message,
                            args
                        ))
                            .colored()
                    } else {
                        "Language node not found: ${level.node()}"
                    }
                }
                else -> {
                    return (asLangText("&f[&d${pluginId}&f]&7(&d&lDEBUG&7)&r " + MessageFormat.format(message, args)))
                        .colored()
                }
            }
        } else {
            return "Language node not found: Level-Prefix"
        }
    }
}

fun ProxyCommandSender.buildInfo(message: String, vararg args: Any): String {
    return buildMessage(LevelType.INFO, message, args)
}

fun ProxyCommandSender.buildWarn(message: String, vararg args: Any): String {
    return buildMessage(LevelType.WARN, message, args)
}

fun ProxyCommandSender.buildError(message: String, vararg args: Any): String {
    return buildMessage(LevelType.ERROR, message, args)
}

fun ProxyCommandSender.buildDebug(message: String, vararg args: Any): String {
    return buildMessage(LevelType.DEBUG, message, args)
}

fun ProxyCommandSender.levelMessage(level: LevelType, message: String, vararg args: Any) {
    sendMessage(buildMessage(level, message, args))
}

fun ProxyCommandSender.levelLocale(level: LevelType, node: String, vararg args: Any) {
    levelMessage(
        level,
        asLangText(
            node,
            "&c&lERROR&7(Encountered language information encountered an error, please contact the administrator to resolve.)",
            args
        )
    )
}

fun ProxyCommandSender.levelLocaleList(level: LevelType, node: String, vararg args: Any) {
    asLangTextList(node, args).forEach { s -> levelMessage(level, s, args) }
}

fun log(message: String, vararg args: Any) {
    console().sendMessage((MessageFormat.format(message, args)).colored())
}

fun log(level: LevelType, message: String, vararg args: Any) {
    console().levelMessage(level, message, args)
}

fun log(level: LevelType, list: List<String>, vararg args: Any) {
    list.forEach { s -> console().levelMessage(level, s, args) }
}

fun logInfo(message: String, vararg args: Any) {
    log(LevelType.INFO, message, args)
}

fun logInfo(list: List<String>, vararg args: Any) {
    list.forEach { s -> logInfo(s, args) }
}

fun logWarn(message: String, vararg args: Any) {
    log(LevelType.WARN, message, args)
}

fun logWarn(list: List<String>, vararg args: Any) {
    list.forEach { s -> logWarn(s, args) }
}

fun logError(message: String, vararg args: Any) {
    log(LevelType.ERROR, message, args)
}

fun logError(list: List<String>, vararg args: Any) {
    list.forEach { s -> logError(s, args) }
}

fun logDebug(message: String, vararg args: Any) {
    log(LevelType.DEBUG, message, args)
}

fun logDebug(list: List<String>, vararg args: Any) {
    list.forEach { s -> logDebug(s, args) }
}

fun logAction(action: String, obj: String, vararg args: Any) {
    logDebug("&7尝试{0} &c{1}&7.", action, MessageFormat.format(obj, args))
}

fun printStackTrace(exception: Throwable, packageFilter: String?) {
    StackTracePrinter.printStackTrace(exception, packageFilter)
}

fun logError(action: String, obj: String, exception: String, vararg args: Any) {
    logError("&7$action &c$obj &7时遇到错误(&c{0}&7).", MessageFormat.format(exception, args))
}

fun logError(action: String, obj: String, e: Throwable, packageFilter: String?) {
    logError(action, obj, e.javaClass.name)
    printStackTrace(e, packageFilter)
}