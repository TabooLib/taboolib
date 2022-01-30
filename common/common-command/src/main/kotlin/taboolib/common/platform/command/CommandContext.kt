package taboolib.common.platform.command

import taboolib.common.platform.ProxyCommandSender

/**
 * @author sky
 * @since 2021/6/25 10:02 上午
 */
class CommandContext<T>(
    val sender: T,
    val command: Command,
    val name: String,
    val compound: Component,
    internal val args: Array<String>,
    internal var index: Int = 0,
) {

    fun checkPermission(permission: String): Boolean {
        return (sender as ProxyCommandSender).hasPermission(permission)
    }

    /**
     * 取全部参数
     */
    fun args(): Array<String> {
        return args.filterIndexed { i, _ -> i <= index }.toTypedArray().also {
            it[index] = "${it[index]} ${args.filterIndexed { i, _ -> i > index }.joinToString(" ")}".trim()
        }
    }

    /**
     * 取绝对位置参数
     */
    fun get(index: Int): String {
        return args()[index]
    }

    fun getOrNull(index: Int): String? {
        return kotlin.runCatching { get(index) }.getOrNull()
    }

    /**
     * 取相对位置参数
     */
    fun argument(offset: Int): String {
        return args()[index + offset]
    }

    fun argumentOrNull(offset: Int): String? {
        return kotlin.runCatching { argument(offset) }.getOrNull()
    }
}