@file:Isolated
package taboolib.common.util

import taboolib.common.Isolated
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer

fun ProxyCommandSender?.isConsole(): Boolean {
    return this !is ProxyPlayer
}