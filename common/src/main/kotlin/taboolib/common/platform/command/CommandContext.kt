package taboolib.common.platform.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptCommandSender

/**
 * TabooLib
 * taboolib.module.command.CommandContext
 *
 * @author sky
 * @since 2021/6/25 10:02 上午
 */
class CommandContext<T>(
    internal val sender: T,
    val command: CommandStructure,
    val name: String,
    val commandCompound: CommandBuilder.CommandBase,
    internal val args: Array<String>,
    internal var index: Int = 0,
) {

    /**
     * 获取命令发送者
     *
     * @return 命令发送者
     */
    fun sender(): ProxyCommandSender {
        return if (sender is ProxyCommandSender) sender else adaptCommandSender(sender as Any)
    }

    /**
     * 检查命令发送者是否持有权限
     *
     * @param permission 权限
     * @return 是否持有权限
     */
    fun checkPermission(permission: String): Boolean {
        return sender().hasPermission(permission)
    }

    /**
     * 取全部参数
     *
     * @return 全部参数
     */
    fun args(): Array<String> {
        return args.filterIndexed { i, _ -> i <= index }.toTypedArray().also {
            it[index] = "${it[index]} ${args.filterIndexed { i, _ -> i > index }.joinToString(" ")}".trim()
        }
    }

    /**
     * 取绝对位置参数
     *
     * @param index 索引
     * @return 参数
     * @throws IndexOutOfBoundsException 索引越界
     */
    fun get(index: Int): String {
        return args()[index]
    }

    /**
     * 取相对位置参数（可为空）
     *
     * @param index 索引
     * @return 参数
     */
    fun getOrNull(index: Int): String? {
        return kotlin.runCatching { get(index) }.getOrNull()
    }

    /**
     * 取相对位置参数
     *
     * @param offset 偏移量
     * @return 参数
     * @throws IndexOutOfBoundsException 索引越界
     */
    fun argument(offset: Int): String {
        return args()[index + offset]
    }

    /**
     * 取相对位置参数（可为空）
     *
     * @param offset 偏移量
     * @return 参数
     * @throws IndexOutOfBoundsException 索引越界
     */
    fun argumentOrNull(offset: Int): String? {
        return kotlin.runCatching { argument(offset) }.getOrNull()
    }
}