package taboolib.platform

import com.velocitypowered.api.command.SimpleCommand
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandInfo
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.service.PlatformCommand
import taboolib.internal.Internal

/**
 * TabooLib
 * taboolib.platform.VelocityCommand
 *
 * @author sky
 * @since 2021/7/4 2:39 下午
 */
@Internal
@Awake
@PlatformSide([Platform.VELOCITY])
class VelocityCommand : PlatformCommand {

    val registeredCommands = ArrayList<String>()

    override fun registerCommand(command: CommandInfo, executor: CommandExecutor, completer: CommandCompleter, component: taboolib.common.platform.command.Component.() -> Unit) {
        registeredCommands.add(command.name)
        VelocityPlugin.getInstance().server.commandManager.register(command.name, object : SimpleCommand {

            override fun execute(invocation: SimpleCommand.Invocation) {
                executor.execute(adaptCommandSender(invocation.source()), command, command.name, invocation.arguments())
            }

            override fun suggest(invocation: SimpleCommand.Invocation): MutableList<String> {
                return completer.execute(
                    adaptCommandSender(invocation.source()),
                    command,
                    command.name,
                    invocation.arguments()
                )?.toMutableList() ?: ArrayList()
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