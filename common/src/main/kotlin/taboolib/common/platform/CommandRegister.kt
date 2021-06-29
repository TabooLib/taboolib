package taboolib.common.platform

fun command(
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.FALSE,
    command: Command.BaseCommand.() -> Unit,
) {
    registerCommand(
        CommandStructure(name, aliases, description, usage, permission, permissionMessage, permissionDefault),
        object : CommandExecutor {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean {
                return Command.BaseCommand(sender, CommandBox(true), CommandBox(null)).also {
                    command(it)
                }.execute(sender, CommandContext(command, name, args))
            }
        },
        object : CommandCompleter {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                return Command.BaseCommand(sender, CommandBox(true), CommandBox(null)).also {
                    command(it)
                }.complete(sender, CommandContext(command, name, args))
            }
        }
    )
}