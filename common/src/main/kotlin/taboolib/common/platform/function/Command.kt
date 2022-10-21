package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.service.PlatformCommand

/**
 *  注册一个命令
 *
 *  @param command 命令结构
 *  @param executor 执行器
 *  @param completer 补全器
 *  @param commandBuilder 命令构建器
 */
fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter, commandBuilder: CommandBuilder.CommandBase.() -> Unit) {
    PlatformFactory.getService<PlatformCommand>().registerCommand(command, executor, completer, commandBuilder)
}

/**
 * 注销一个命令
 *
 * @param command 命令结构
 */
fun unregisterCommand(command: CommandStructure) {
    unregisterCommand(command.name)
    command.aliases.forEach { unregisterCommand(it) }
}

/**
 * 注销一个命令
 *
 * @param command 命令名称
 */
fun unregisterCommand(command: String) {
    PlatformFactory.getService<PlatformCommand>().unregisterCommand(command)
}

/**
 * 注销所有命令
 */
fun unregisterCommands() {
    PlatformFactory.getService<PlatformCommand>().unregisterCommands()
}