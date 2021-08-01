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
    val languageFile =
        Language.languageFile[getLocale()] ?: Language.languageFile[Language.languageCode.firstOrNull() ?: "zh_CN"]
    if (languageFile == null) {
        return "Language file not found"
    } else {
        val prefix = languageFile.nodes["Plugin.Prefix"]
        if (prefix != null) {
            when (level) {
                LevelType.INFO, LevelType.WARN, LevelType.ERROR -> {
                    val info = languageFile.nodes[level.node()]
                    if (info != null) {
                        return (asLangText("Plugin.Prefix") + asLangText(level.node()) + MessageFormat.format(
                            message,
                            args
                        ))
                            .colored()
                    } else {
                        return "Language node not found: ${level.node()}"
                    }
                }
                else -> {
                    return (asLangText("&f[&d${pluginId}&f]&7(&d&lDEBUG&7)&r " + MessageFormat.format(message, args)))
                        .colored()
                }
            }
        } else {
            return "Language node not found: Plugin.Prefix"
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
    val msg = exception.localizedMessage
    log("&7===================================&c&l printStackTrace &7===================================")
    log("&7Exception Type ▶")
    log("&c" + exception.javaClass.name)
    log("&c" + if (msg == null || msg.isEmpty()) "&7No description." else msg)
    // org.serverct.parrot.plugin.Plugin
    // org.serverct.parrot.plugin.Plugin
    var lastPackage = ""
    for (elem in exception.stackTrace) {
        val key = elem.className
        var pass = true
        if (packageFilter != null) {
            pass = key.contains(packageFilter)
        }
        val nameSet = key.split("[.]").toTypedArray()
        val className = nameSet[nameSet.size - 1]
        val packageSet = arrayOfNulls<String>(nameSet.size - 2)
        System.arraycopy(nameSet, 0, packageSet, 0, nameSet.size - 2)
        val packageName = StringBuilder()
        var counter = 0
        for (nameElem in packageSet) {
            packageName.append(nameElem)
            if (counter < packageSet.size - 1) {
                packageName.append(".")
            }
            counter++
        }
        if (pass) {
            if (packageName.toString() != lastPackage) {
                lastPackage = packageName.toString()
                log("")
                log("&7Package &c$packageName &7▶")
            }
            log("  &7▶ at Class &c" + className + "&7, Method &c" + elem.methodName + "&7. (&c" + elem.fileName + "&7, Line &c" + elem.lineNumber + "&7)")
        }
    }
    log("&7===================================&c&l printStackTrace &7===================================")
}

fun logError(action: String, obj: String, exception: String, vararg args: Any) {
    logError("&7{0} &c{1} &7时遇到错误(&c{2}&7).", action, obj, MessageFormat.format(exception, args))
}

fun logError(action: String, obj: String, e: Throwable, packageFilter: String?) {
    logError(action, obj, e.toString())
    printStackTrace(e, packageFilter)
}