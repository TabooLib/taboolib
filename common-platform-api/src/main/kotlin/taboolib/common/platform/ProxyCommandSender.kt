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

    /**
     * 获取原始对象
     */
    val origin: Any

    /**
     * 名称
     */
    val name: String

    /**
     * 是否 OP
     */
    var isOp: Boolean

    /**
     * 是否在线
     */
    fun isOnline(): Boolean

    /**
     * 发送消息
     *
     * @param message 消息
     */
    fun sendMessage(message: String)

    /**
     * 执行命令
     *
     * @param command 命令
     * @return 是否成功
     */
    fun performCommand(command: String): Boolean

    /**
     * 是否持有权限
     *
     * @param permission 权限
     * @return 是否持有
     */
    fun hasPermission(permission: String): Boolean

    /**
     * 转换到指定类型
     *
     * @throws ClassCastException
     */
    fun <T> cast(): T {
        return origin as T
    }

    /**
     * 安全转换到指定类型
     */
    fun <T> castSafely(): T? {
        return origin as? T
    }
}