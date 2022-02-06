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
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.service.PlatformCommand
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.platform.command.*
import taboolib.internal.Internal

/**
 * TabooLib
 * taboolib.platform.BungeeCommand
 *
 * @author sky
 * @since 2021/7/3 1:03 上午
 */
@Internal
@Awake
@PlatformSide([Platform.BUNGEE])
class BungeeCommand : PlatformCommand {

    val plugin: BungeePlugin
        get() = BungeePlugin.getInstance()

    val commandMap by lazy {
        BungeeCord.getInstance().pluginManager.getProperty<MutableMap<String, Command>>("commandMap")!!
    }

    override fun registerCommand(command: CommandInfo, executor: CommandExecutor, completer: CommandCompleter, component: Component.() -> Unit) {
        BungeeCord.getInstance().pluginManager.registerCommand(
            BungeePlugin.getInstance(),
            object : Command(command.name, command.permission.ifEmpty { "${plugin.description.name}.command.use" }), TabExecutor {

                override fun execute(sender: CommandSender, args: Array<String>) {
                    executor.execute(adaptCommandSender(sender), command, command.name, args)
                }

                override fun onTabComplete(sender: CommandSender, args: Array<String>): MutableIterable<String> {
                    return completer.execute(adaptCommandSender(sender), command, command.name, args)?.toMutableList() ?: ArrayList()
                }
            })
    }

    override fun unregisterCommand(command: String) {
        BungeeCord.getInstance().pluginManager.unregisterCommand(commandMap[command] ?: return)
    }

    override fun unregisterCommands() {
        BungeeCord.getInstance().pluginManager.unregisterCommands(BungeePlugin.getInstance())
    }
}