package taboolib.platform

import taboolib.common.platform.*

/**
 * TabooLib
 * taboolib.platform.VelocityCommand
 *
 * @author sky
 * @since 2021/7/4 2:39 下午
 */
@PlatformSide([Platform.VELOCITY])
class VelocityCommand : PlatformCommand {

    override fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter) {
        TODO("Not yet implemented")
    }

    override fun unregisterCommand(command: String) {
        TODO("Not yet implemented")
    }

    override fun unregisterCommands() {
        TODO("Not yet implemented")
    }
}