package taboolib.platform

import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.TranslatableComponent
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.Inject
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.service.PlatformCommand

/**
 * TabooLib
 * taboolib.platform.BungeeCommand
 *
 * @author sky
 * @since 2021/7/3 1:03 上午
 */
@Awake
@Inject
@PlatformSide(Platform.BUNGEE)
class BungeeCommand : PlatformCommand {

    val plugin: BungeePlugin
        get() = BungeePlugin.getInstance()

    override fun registerCommand(
        command: CommandStructure,
        executor: CommandExecutor,
        completer: CommandCompleter,
        commandBuilder: CommandBase.() -> Unit,
    ) {
        val permission = command.permission.ifEmpty { "${plugin.description.name}.command.use" }
        val registeredCommand = RegisteredBungeeCommand(
            command.name,
            permission,
            command.aliases,
            execute = { sender, args -> executor.execute(adaptCommandSender(sender), command, command.name, args) },
            complete = { sender, args ->
                completer.execute(adaptCommandSender(sender), command, command.name, args)?.toMutableList() ?: ArrayList()
            }
        )
        BungeeCord.getInstance().pluginManager.registerCommand(BungeePlugin.getInstance(), registeredCommand)
    }

    override fun unregisterCommand(command: String) {
        val instance = BungeeCord.getInstance().pluginManager.getProperty<MutableMap<String, Command>>("commandMap")?.get(command) ?: return
        BungeeCord.getInstance().pluginManager.unregisterCommand(instance)
    }

    override fun unregisterCommands() {
        BungeeCord.getInstance().pluginManager.unregisterCommands(BungeePlugin.getInstance())
    }

    override fun unknownCommand(sender: ProxyCommandSender, command: String, state: Int) {
        when (state) {
            1 -> sender.cast<CommandSender>().sendMessage(TranslatableComponent("command.unknown.command").also {
                it.color = ChatColor.RED
            })
            2 -> sender.cast<CommandSender>().sendMessage(TranslatableComponent("command.unknown.argument").also {
                it.color = ChatColor.RED
            })
            else -> return
        }
        val components = ArrayList<BaseComponent>()
        components += TextComponent(command)
        components += TranslatableComponent("command.context.here").also {
            it.color = ChatColor.RED
            it.isItalic = true
        }
        sender.cast<CommandSender>().sendMessage(*components.toTypedArray())
    }
}

private class RegisteredBungeeCommand(
    name: String,
    permission: String,
    aliases: List<String>,
    private val execute: (CommandSender, Array<String>) -> Unit,
    private val complete: (CommandSender, Array<String>) -> MutableIterable<String>,
) : Command(name, permission, *aliases.toTypedArray()), TabExecutor {

    override fun execute(sender: CommandSender, args: Array<String>) {
        execute.invoke(sender, args)
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): MutableIterable<String> {
        return complete.invoke(sender, args)
    }
}
