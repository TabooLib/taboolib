package taboolib.platform

import taboolib.common.platform.*

/**
 * TabooLib
 * taboolib.platform.SpongeCommand
 *
 * @author sky
 * @since 2021/7/4 2:45 下午
 */
@PlatformSide([Platform.SPONGE_API_8])
class SpongeCommand : PlatformCommand {

    override fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter) {
    }

    override fun unregisterCommand(command: String) {
    }

    override fun unregisterCommands() {
    }
}