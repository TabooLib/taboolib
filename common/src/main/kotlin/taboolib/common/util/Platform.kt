@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common.util

import taboolib.common.Isolated
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer

/**
 * 是否为控制台对象
 */
inline fun ProxyCommandSender?.isConsole(): Boolean {
    return this !is ProxyPlayer
}