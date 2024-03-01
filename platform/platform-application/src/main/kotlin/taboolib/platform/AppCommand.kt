package taboolib.platform

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

/**
 * @author Score2
 * @since 2022/06/07 23:43
 */
@Awake
@PlatformSide(Platform.APPLICATION)
class AppCommand : PlatformCommand {

    companion object {

        val unknownCommandMessage
            get() = System.getProperty("taboolib.application.command.unknown.message")
                ?: "Unknown command.".apply { System.setProperty("taboolib.application.command.unknown.message", this) }

        val commands = mutableSetOf<Command>()

        fun register(command: Command) {
            commands.add(command)
        }

        fun unregister(name: String) {
            commands.find { it.command.aliases.contains(name) } ?: return
            unregister(name)
        }

        fun unregister(command: Command) {
            commands.remove(command)
        }

        fun runCommand(content: String) {
            if (content.isBlank()) {
                return
            }
            val label = if (content.contains(" ")) content.substringBefore(" ") else content
            val command = commands.find { it.aliases.contains(label) } ?: return info(unknownCommandMessage)
            val args = if (content.contains(" ")) content.substringAfter(" ").split(" ") else listOf()
            command.executor.execute(AppConsole, command.command, label, args.toTypedArray())
        }

        fun suggest(content: String): List<String> {
            fun suggestion() = commands.flatMap { it.aliases }
            if (content.isBlank()) {
                return suggestion()
            }
            val label = if (content.contains(" ")) content.substringBefore(" ") else content
            val command = commands.find { it.aliases.contains(label) } ?: return suggestion().filter { it.startsWith(label) }
            return if (content.contains(" ")) {
                command.completer.execute(AppConsole, command.command, label, content.substringAfter(" ").split(" ").toTypedArray()) ?: listOf()
            } else {
                listOf()
            }
        }
    }


    data class Command(val command: CommandStructure, val executor: CommandExecutor, val completer: CommandCompleter, val commandBuilder: CommandBase.() -> Unit) {

        val aliases get() = listOf(command.name, *command.aliases.toTypedArray())

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
        unregister(commands.find { it.command.aliases.contains(command) } ?: return)
    }

    override fun unregisterCommands() {
        commands.forEach { unregister(it) }
    }
}