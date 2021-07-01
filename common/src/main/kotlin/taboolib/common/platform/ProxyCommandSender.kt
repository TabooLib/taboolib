package taboolib.common.platform

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

    var isOp: Boolean

    fun sendMessage(message: String)

    fun performCommand(command: String): Boolean

    fun hasPermission(permission: String): Boolean

    fun <T> cast(type: Class<T>): T {
        return type.cast(origin)
    }
}