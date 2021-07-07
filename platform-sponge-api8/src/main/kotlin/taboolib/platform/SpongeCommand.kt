package taboolib.platform

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandCallable
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import taboolib.common.platform.*
import java.util.*

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
//        Sponge.getCommandManager().register(SpongePlugin.getInstance(),
//            CommandSpec.builder()
//                .description(Text.of(command.description))
//
//        )
    }

    override fun unregisterCommand(command: String) {
    }

    override fun unregisterCommands() {
    }
}