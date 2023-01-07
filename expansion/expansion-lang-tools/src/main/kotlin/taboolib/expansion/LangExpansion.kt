package taboolib.expansion

import org.bukkit.command.CommandSender
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.module.chat.colored
import taboolib.platform.util.asLangText
import taboolib.platform.util.bukkitPlugin

private val consoleSender = bukkitPlugin.server.consoleSender


/**
 * Info as lang
 * 国际化普通消息
 *
 * @param node 节点
 * @author Bingzi
 */
fun infoAsLang(node: String) {
    info(consoleSender.asLangText(node).colored())
}

/**
 * Info as lang
 * 国际化普通消息
 *
 * @param node 节点
 * @param args 参数
 * @author Bingzi
 */
fun infoAsLang(node: String, vararg args: Any) {
    info(consoleSender.asLangText(node, *args).colored())
}

/**
 * Warning as lang
 * 国际化警告消息
 *
 * @param node 节点
 * @author Bingzi
 */
fun warningAsLang(node: String) {
    warning(consoleSender.asLangText(node).colored())
}

/**
 * Warning as lang
 * 国际化警告消息
 *
 * @param node 节点
 * @param args 参数
 * @author Bingzi
 */
fun warningAsLang(node: String, vararg args: Any) {
    warning(consoleSender.asLangText(node, *args).colored())
}

/**
 * Send message as lang
 * 国际化文本消息
 *
 * @param node 节点
 * @author Bingzi
 */
fun CommandSender.sendMessageAsLang(node: String) {
    this.sendMessage(this.asLangText(node).colored())
}

/**
 * Send message as lang
 * 国际化文本消息
 *
 * @param node 节点
 * @param args 参数
 * @author Bingzi
 */
fun CommandSender.sendMessageAsLang(node: String, vararg args: Any) {
    this.sendMessage(this.asLangText(node, *args).colored())
}