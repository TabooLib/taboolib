package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.service.PlatformCommand

fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter, commandBuilder: CommandBuilder.CommandBase.() -> Unit) {
    PlatformFactory.getPlatformService<PlatformCommand>().registerCommand(command, executor, completer, commandBuilder)
}

fun unregisterCommand(command: CommandStructure) {
    unregisterCommand(command.name)
    command.aliases.forEach { unregisterCommand(it) }
}

fun unregisterCommand(command: String) {
    PlatformFactory.getPlatformService<PlatformCommand>().unregisterCommand(command)
}

fun unregisterCommands() {
    PlatformFactory.getPlatformService<PlatformCommand>().unregisterCommands()
}