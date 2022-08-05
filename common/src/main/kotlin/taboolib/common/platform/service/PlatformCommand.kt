package taboolib.common.platform.service

import taboolib.common.platform.PlatformService
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure

/**
 * TabooLib
 * taboolib.common.platform.service.PlatformCommand
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

    fun isSupportedUnknownCommand() = false

    companion object {

        const val defaultPermissionMessage = "§cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error."
    }
}