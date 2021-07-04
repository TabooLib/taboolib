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


    // TODO: NEED FIX!!
    override fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter) {
//        Sponge.getCommandManager().register(SpongePlugin.getInstance(),
//            CommandSpec.builder()
//                .description(Text.of(command.description))
//
//        )
        Sponge.getCommandManager().register(SpongePlugin.getInstance(), object : CommandCallable {

            override fun process(source: CommandSource, arguments: String): CommandResult {
                // Maybe it cause some problems.
                executor.execute(adaptCommandSender(source), command, command.name, arguments.map { it.toString() }.toTypedArray())
                return CommandResult.success()
            }

            override fun getSuggestions(source: CommandSource, arguments: String, targetPosition: Location<World>?): MutableList<String> {
                return completer.execute(adaptCommandSender(source), command, command.name, arguments.map { it.toString() }.toTypedArray())?.toMutableList()
                    ?: arrayListOf()
            }

            override fun testPermission(source: CommandSource): Boolean {
                return source.hasPermission(command.permission)
            }

            override fun getShortDescription(source: CommandSource): Optional<Text> {
                return Optional.of(Text.of(command.description))
            }

            override fun getHelp(source: CommandSource): Optional<Text> {
                // wait.. what the fuck? a help message from a command ?
                return Optional.of(Text.of("None"))
            }

            override fun getUsage(source: CommandSource): Text {
                return Text.of(command.usage)
            }
        })
    }

    override fun unregisterCommand(command: String) {
        Sponge.getCommandManager().commands.filter { it.primaryAlias == command }.onEach {
            Sponge.getCommandManager().removeMapping(it)
        }
    }

    override fun unregisterCommands() {
        Sponge.getCommandManager().getOwnedBy(SpongePlugin.getInstance()).onEach {
            Sponge.getCommandManager().removeMapping(it)
        }
    }
}