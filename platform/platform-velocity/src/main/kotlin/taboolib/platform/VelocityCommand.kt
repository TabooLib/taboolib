package taboolib.platform

import com.velocitypowered.api.command.SimpleCommand
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.service.PlatformCommand

/**
 * TabooLib
 * taboolib.platform.VelocityCommand
 *
 * @author sky
 * @since 2021/7/4 2:39 下午
 */
@Awake
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
        VelocityPlugin.getInstance().server.commandManager.register(command.name, object : SimpleCommand {

            override fun execute(invocation: SimpleCommand.Invocation) {
                executor.execute(adaptCommandSender(invocation.source()), command, command.name, invocation.arguments())
            }

            override fun suggest(invocation: SimpleCommand.Invocation): MutableList<String> {
                return completer.execute(adaptCommandSender(invocation.source()), command, command.name, invocation.arguments())?.toMutableList() ?: ArrayList()
            }
        }, *command.aliases.toTypedArray())
    }

    override fun unregisterCommand(command: String) {
        VelocityPlugin.getInstance().server.commandManager.unregister(command)
    }

    override fun unregisterCommands() {
        registeredCommands.onEach { VelocityPlugin.getInstance().server.commandManager.unregister(it) }
    }

    @Suppress("DEPRECATION")
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