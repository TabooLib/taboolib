package taboolib.common.platform

/**
 * TabooLib
 * taboolib.common.platform.ProxyCommandSender
 *
 * @author sky
 * @since 2021/6/17 12:03 上午
 */
@Suppress("UNCHECKED_CAST")
interface ProxyCommandSender {

    val origin: Any

    val name: String

    var isOp: Boolean

    fun isOnline(): Boolean

    fun sendMessage(message: String)

    fun performCommand(command: String): Boolean

    fun hasPermission(permission: String): Boolean

    fun <T> cast(): T {
        return origin as T
    }

    fun <T> castSafely(): T? {
        return origin as? T
    }
}
