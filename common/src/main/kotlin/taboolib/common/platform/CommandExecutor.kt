package taboolib.common.platform

/**
 * TabooLib
 * taboolib.common.CommandExecutor
 *
 * @author sky
 * @since 2021/6/24 11:49 下午
 */
interface CommandExecutor {

    fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean
}