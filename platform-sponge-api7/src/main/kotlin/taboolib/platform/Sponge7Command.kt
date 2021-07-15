package taboolib.platform

import org.spongepowered.api.Sponge
import taboolib.common.platform.*

/**
 * TabooLib
 * taboolib.platform.SpongeCommand
 *
 * @author sky
 * @since 2021/7/4 2:45 下午
 */
@PlatformSide([Platform.SPONGE_API_7])
class Sponge7Command : PlatformCommand {

    override fun registerCommand(
        command: CommandStructure,
        executor: CommandExecutor,
        completer: CommandCompleter,
        commandBuilder: Command.BaseCommand.() -> Unit,
    ) {
    }

    override fun unregisterCommand(command: String) {
        Sponge.getCommandManager().commands.filter { it.primaryAlias == command }.onEach {
            Sponge.getCommandManager().removeMapping(it)
        }
    }

    override fun unregisterCommands() {
        Sponge.getCommandManager().getOwnedBy(Sponge7Plugin.getInstance()).onEach {
            Sponge.getCommandManager().removeMapping(it)
        }
    }
}