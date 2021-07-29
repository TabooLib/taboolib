@file:Isolated

package taboolib.platform.util

import org.bukkit.command.CommandSender
import taboolib.common.Isolated
import taboolib.common.platform.adaptCommandSender
import taboolib.module.lang.sendLang

fun CommandSender.sendLang(node: String, vararg args: Any) {
    adaptCommandSender(this).sendLang(node, *args)
}