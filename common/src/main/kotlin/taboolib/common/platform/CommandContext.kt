package taboolib.common.platform

/**
 * TabooLib
 * taboolib.module.command.CommandContext
 *
 * @author sky
 * @since 2021/6/25 10:02 上午
 */
class CommandContext<T>(
    val sender: T,
    val command: CommandStructure,
    val name: String,
    internal val args: Array<String>,
    internal var index: Int = 0,
) {

    fun checkPermission(permission: String): Boolean {
        return (sender as ProxyCommandSender).hasPermission(permission)
    }

    fun argument(offset: Int): String? {
        val args = args.filterIndexed { i, _ -> i <= index }.toTypedArray().also {
            it[index] = "${it[index]} ${args.filterIndexed { i, _ -> i > index }.joinToString(" ")}".trim()
        }
        return args.getOrNull(index + offset)
    }
}