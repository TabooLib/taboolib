package taboolib.platform

import taboolib.common.Inject
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.function.info
import taboolib.common.platform.service.PlatformCommand
import java.util.concurrent.CopyOnWriteArraySet

/**
 * @author Score2
 * @since 2022/06/07 23:43
 */
@Awake
@Inject
@PlatformSide(Platform.APPLICATION)
class AppCommand : PlatformCommand {

    companion object {

        val unknownCommandMessage: String
            get() = System.getProperty("taboolib.application.command.unknown.message") ?: "Unknown command."

        val commands: MutableSet<Command> = CopyOnWriteArraySet()

        fun register(command: Command) {
            commands.add(command)
        }

        fun unregister(name: String) {
            commands.removeIf { it.matches(name) }
        }

        fun unregister(command: Command) {
            commands.remove(command)
        }

        fun runCommand(content: String) {
            if (content.isBlank()) {
                return
            }
            val label = if (content.contains(" ")) content.substringBefore(" ") else content
            val command = commands.find { it.matches(label) } ?: return info(unknownCommandMessage)
            val args = if (content.contains(" ")) content.substringAfter(" ").split(" ") else listOf()
            command.executor.execute(AppConsole, command.command, label, args.toTypedArray())
        }

        fun suggest(content: String): List<String> {
            fun suggestion() = commands.flatMap { it.aliases }
            if (content.isBlank()) {
                return suggestion()
            }
            val label = if (content.contains(" ")) content.substringBefore(" ") else content
            val command = commands.find { it.matches(label) } ?: return suggestion().filter { it.startsWith(label, ignoreCase = true) }
            return if (content.contains(" ")) {
                command.completer.execute(AppConsole, command.command, label, content.substringAfter(" ").split(" ").toTypedArray()) ?: listOf()
            } else {
                listOf()
            }
        }
    }


    data class Command(val command: CommandStructure, val executor: CommandExecutor, val completer: CommandCompleter, val commandBuilder: CommandBase.() -> Unit) {

        val aliases get() = listOf(command.name, *command.aliases.toTypedArray())

        fun matches(name: String) = aliases.any { it.equals(name, ignoreCase = true) }

        fun register() = register(this)

        fun unregister() = unregister(this)
    }

    override fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter, commandBuilder: CommandBase.() -> Unit) {
        register(Command(command, executor, completer, commandBuilder))
    }

    override fun unknownCommand(sender: ProxyCommandSender, command: String, state: Int) {
        sender.sendMessage("$command<--[HERE]")
    }

    override fun unregisterCommand(command: String) {
        unregister(command)
    }

    override fun unregisterCommands() {
        commands.clear()
    }
}