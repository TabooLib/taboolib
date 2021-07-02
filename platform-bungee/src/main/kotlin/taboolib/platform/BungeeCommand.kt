package taboolib.platform

import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import taboolib.common.platform.*

/**
 * TabooLib
 * taboolib.platform.BungeeCommand
 *
 * @author sky
 * @since 2021/7/3 1:03 上午
 */
@PlatformSide([Platform.BUNGEE])
class BungeeCommand : PlatformCommand {

    @Suppress("ObjectLiteralToLambda")
    override fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter) {
        BungeeCord.getInstance().pluginManager.registerCommand(BungeePlugin.getInstance(), object : Command(command.name), TabExecutor {

            override fun execute(sender: CommandSender, args: Array<String>) {
                executor.execute(adaptCommandSender(sender), command, command.name, args)
            }

            override fun onTabComplete(sender: CommandSender, args: Array<String>): MutableIterable<String> {
                return completer.execute(adaptCommandSender(sender), command, command.name, args)?.toMutableList() ?: arrayListOf()
            }
        })
    }

    override fun unregisterCommand(command: String) {
        BungeeCord.getInstance().pluginManager.commands.filter { it.key == command }.forEach {
            BungeeCord.getInstance().pluginManager.unregisterCommand(it.value)
        }
    }

    override fun unregisterCommands() {
        BungeeCord.getInstance().pluginManager.unregisterListeners(BungeePlugin.getInstance())
    }
}