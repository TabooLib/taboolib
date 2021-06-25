package taboolib.module.command

import taboolib.common.platform.*

fun command(
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.FALSE,
    command: Command.BaseCommand.() -> Unit
) {
    registerCommand(
        Command(name, aliases, description, usage, permission, permissionMessage, permissionDefault),
        object : CommandExecutor {

            override fun execute(sender: ProxyCommandSender, command: taboolib.common.platform.Command, name: String, args: Array<String>): Boolean {
                return Command.BaseCommand(sender, CommandBox(true), CommandBox(null)).also {
                    command(it)
                }.execute(sender, CommandContext(command, name, args))
            }
        },
        object : CommandTabCompleter {

            override fun execute(sender: ProxyCommandSender, command: taboolib.common.platform.Command, name: String, args: Array<String>): List<String>? {
                return Command.BaseCommand(sender, CommandBox(true), CommandBox(null)).also {
                    command(it)
                }.complete(sender, CommandContext(command, name, args))
            }
        }
    )
}