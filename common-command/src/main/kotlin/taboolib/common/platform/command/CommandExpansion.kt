package taboolib.common.platform.command

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.service.PlatformCommand

fun command(
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.OP,
    permissionChildren: Map<String, PermissionDefault> = emptyMap(),
    commandBuilder: Component.() -> Unit,
) {
    PlatformFactory.getPlatformService<PlatformCommand>().registerCommand(
        CommandInfo(name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren),
        object : CommandExecutor {

            override fun execute(sender: ProxyCommandSender, command: CommandInfo, name: String, args: Array<String>): Boolean {
                val compound = Component.INSTANCE.createCompound().also(commandBuilder)
                return compound.execute(CommandContext(sender, command, name, compound, args))
            }
        },
        object : CommandCompleter {

            override fun execute(sender: ProxyCommandSender, command: CommandInfo, name: String, args: Array<String>): List<String>? {
                val compound = Component.INSTANCE.createCompound().also(commandBuilder)
                return compound.suggest(CommandContext(sender, command, name, compound, args))
            }
        },
        commandBuilder
    )
}
