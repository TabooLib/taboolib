package taboolib.common.platform.command

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.service.PlatformCommand
import java.util.stream.Stream

@Suppress("LongParameterList")
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
    val info = CommandInfo(name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren)
    val executor = object : CommandExecutor {

        override fun execute(sender: ProxyCommandSender, command: CommandInfo, name: String, args: Array<String>): Boolean {
            val compound = Component.INSTANCE.createCompound().also(commandBuilder)
            return compound.execute(CommandContext(sender, command, name, compound, args))
        }
    }
    val completer = object : CommandCompleter {

        override fun execute(sender: ProxyCommandSender, command: CommandInfo, name: String, args: Array<String>): List<String>? {
            val compound = Component.INSTANCE.createCompound().also(commandBuilder)
            return compound.suggest(CommandContext(sender, command, name, compound, args))
        }
    }
    PlatformFactory.getPlatformService<PlatformCommand>().registerCommand(info, executor, completer, commandBuilder)
}

internal inline fun <reified T> Stream<T>.toTypedArray() = toArray<T> { arrayOfNulls(0) }
