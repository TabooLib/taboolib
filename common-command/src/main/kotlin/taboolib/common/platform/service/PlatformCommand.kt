package taboolib.common.platform.service

import taboolib.common.platform.PlatformService
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*

/**
 * @author sky
 * @since 2021/6/24 11:46 下午
 */
@PlatformService
interface PlatformCommand {

    fun registerCommand(command: CommandInfo, executor: CommandExecutor, completer: CommandCompleter, component: Component.() -> Unit)

    fun unregisterCommand(command: String)

    fun unregisterCommands()

    companion object {

        const val defaultPermissionMessage = "§cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error."
    }
}