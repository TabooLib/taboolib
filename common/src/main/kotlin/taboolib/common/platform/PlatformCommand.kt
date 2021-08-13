package taboolib.common.platform

/**
 * TabooLib
 * taboolib.common.platform.PlatformCommand
 *
 * @author sky
 * @since 2021/6/24 11:46 下午
 */
@PlatformService
interface PlatformCommand {

    fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter, commandBuilder: CommandBuilder.CommandBase.() -> Unit)

    fun unregisterCommand(command: String)

    fun unregisterCommands()

    fun unknownCommand(sender: ProxyCommandSender, command: String, state: Int)

    companion object {

        const val defaultPermissionMessage = "§cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error."
    }
}