package taboolib.platform

import com.velocitypowered.api.command.Command
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
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
        commandBuilder: CommandBuilder.CommandBase.() -> Unit,
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

    override fun unknownCommand(sender: ProxyCommandSender, command: String, state: Int) {
        when (state) {
            1 -> sender.cast<Audience>().sendMessage(Component.translatable("command.unknown.command", TextColor.color(0xFF5555)))
            2 -> sender.cast<Audience>().sendMessage(Component.translatable("command.unknown.command", TextColor.color(0xFF5555)))
            else -> return
        }
        val components = ArrayList<Component>()
        components += Component.text(command)
        components += Component.translatable("command.context.here", TextColor.color(0xFF5555), TextDecoration.ITALIC)
        sender.cast<Audience>().sendMessage(Component.join(Component.empty(), components))
    }
}