package taboolib.platform

import com.velocitypowered.api.command.Command
import com.velocitypowered.api.command.CommandSource
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

    val registeredCommands = ArrayList<String>()

    override fun registerCommand(
        command: CommandStructure,
        executor: CommandExecutor,
        completer: CommandCompleter,
        commandBuilder: taboolib.common.platform.CommandBuilder.CommandBase.() -> Unit,
    ) {
        registeredCommands.add(command.name)
        VelocityPlugin.getInstance().server.commandManager.register(command.name, object : Command {

            override fun execute(source: CommandSource, args: Array<String>) {
                executor.execute(adaptCommandSender(source), command, command.name, args)
            }

            override fun suggest(source: CommandSource, currentArgs: Array<String>): MutableList<String> {
                return completer.execute(adaptCommandSender(source), command, command.name, currentArgs)?.toMutableList() ?: ArrayList()
            }
        }, *command.aliases.toTypedArray())
    }

    override fun unregisterCommand(command: String) {
        VelocityPlugin.getInstance().server.commandManager.unregister(command)
    }

    override fun unregisterCommands() {
        registeredCommands.onEach { VelocityPlugin.getInstance().server.commandManager.unregister(it) }
    }
}