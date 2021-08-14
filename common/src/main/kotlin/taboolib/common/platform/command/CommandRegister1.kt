package taboolib.common.platform.command

import taboolib.common.platform.PlatformFactory

fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter, commandBuilder: CommandBuilder.CommandBase.() -> Unit) {
    PlatformFactory.getService<PlatformCommand>().registerCommand(command, executor, completer, commandBuilder)
}

fun unregisterCommand(command: CommandStructure) {
    unregisterCommand(command.name)
    command.aliases.forEach { unregisterCommand(it) }
}

fun unregisterCommand(command: String) {
    PlatformFactory.getService<PlatformCommand>().unregisterCommand(command)
}

fun unregisterCommands() {
    PlatformFactory.getService<PlatformCommand>().unregisterCommands()
}