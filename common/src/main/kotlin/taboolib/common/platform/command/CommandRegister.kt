package taboolib.common.platform.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.registerCommand

fun command(
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.OP,
    permissionChildren: Map<String, PermissionDefault> = emptyMap(),
    commandBuilder: CommandBuilder.CommandBase.() -> Unit,
) {
    registerCommand(
        CommandStructure(name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren),
        object : CommandExecutor {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean {
                return CommandBuilder.CommandBase().also(commandBuilder).execute(CommandContext(sender, command, name, args))
            }
        },
        object : CommandCompleter {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                return CommandBuilder.CommandBase().also(commandBuilder).suggest(CommandContext(sender, command, name, args))
            }
        },
        commandBuilder
    )
}
