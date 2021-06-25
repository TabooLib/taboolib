package taboolib.common.platform

import taboolib.common.util.Location

/**
 * TabooLib
 * taboolib.common.platform.ProxyCommandSender
 *
 * @author sky
 * @since 2021/6/17 12:03 上午
 */
interface ProxyCommandSender {

    val origin: Any

    val name: String

    fun sendMessage(message: String)

    fun performCommand(command: String): Boolean

    fun hasPermission(permission: String): Boolean

    fun <T> cast(type: Class<T>): T {
        return type.cast(origin)
    }
}