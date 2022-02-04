package taboolib.platform

import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.TranslatableComponent
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.service.PlatformCommand
import org.tabooproject.reflex.Reflex.Companion.getProperty

/**
 * TabooLib
 * taboolib.platform.BungeeCommand
 *
 * @author sky
 * @since 2021/7/3 1:03 上午
 */
@Awake
@PlatformSide([Platform.BUNGEE])
class BungeeCommand : PlatformCommand {

    val plugin: BungeePlugin
        get() = BungeePlugin.getInstance()

    override fun registerCommand(
        command: CommandStructure,
        executor: CommandExecutor,
        completer: CommandCompleter,
        commandBuilder: CommandBuilder.CommandBase.() -> Unit,
    ) {
        val permission = command.permission.ifEmpty { "${plugin.description.name}.command.use" }
        BungeeCord.getInstance().pluginManager.registerCommand(BungeePlugin.getInstance(), object : Command(command.name, permission), TabExecutor {

            override fun execute(sender: CommandSender, args: Array<String>) {
                executor.execute(adaptCommandSender(sender), command, command.name, args)
            }

            override fun onTabComplete(sender: CommandSender, args: Array<String>): MutableIterable<String> {
                return completer.execute(adaptCommandSender(sender), command, command.name, args)?.toMutableList() ?: ArrayList()
            }
        })
    }

    override fun unregisterCommand(command: String) {
        val instance = BungeeCord.getInstance().pluginManager.getProperty<MutableMap<String, Command>>("commandMap")!![command] ?: return
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