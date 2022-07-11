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

    fun sender(): ProxyCommandSender {
        return if (sender is ProxyCommandSender) sender else adaptCommandSender(sender as Any)
    }

    fun checkPermission(permission: String): Boolean {
        return sender().hasPermission(permission)
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