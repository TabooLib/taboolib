package taboolib.platform

import taboolib.common.platform.*

/**
 * TabooLib
 * taboolib.platform.SpongeCommand
 *
 * @author sky
 * @since 2021/7/4 2:45 下午
 */
@PlatformSide([Platform.SPONGE])
class SpongeCommand : PlatformCommand {

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