package taboolib.platform

import cn.nukkit.Server
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.service.PlatformCommand
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.util.unsafeLazy

/**
 * TabooLib
 * starslib.platform.NukkitCommand
 *
 * @author sky
 * @since 2021/7/3 1:15 上午
 */
@Awake
@PlatformSide([Platform.NUKKIT])
class NukkitCommand : PlatformCommand {

    val plugin: NukkitPlugin
        get() = NukkitPlugin.getInstance()

    val knownCommands = ArrayList<CommandStructure>()

    val registeredCommands by unsafeLazy {
        Server.getInstance().commandMap.commands
    }

    override fun registerCommand(
        command: CommandStructure,
        executor: CommandExecutor,
        completer: CommandCompleter,
        commandBuilder: CommandBase.() -> Unit,
    ) {
        // TODO: 2021/7/15 Not Support Suggestions


        val registerCommand = object : Command(command.name, command.description.ifEmpty { command.name }, command.usage) {
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
                return executor.execute(adaptCommandSender(sender), command, commandLabel, args)
            }
        }

        registerCommand.permission = command.permission.ifEmpty { "${plugin.name}.command.use" }
        registerCommand.permissionMessage = command.permissionMessage.ifEmpty { PlatformCommand.defaultPermissionMessage }

        knownCommands += command
        registeredCommands[command.name] = registerCommand
        command.aliases.forEach { registeredCommands[it] = registerCommand }
    }

    override fun unregisterCommand(command: String) {
        registeredCommands.remove(command)
    }

    override fun unregisterCommands() {
        knownCommands.forEach { registeredCommands.remove(it.name) }
    }

    override fun unknownCommand(sender: ProxyCommandSender, command: String, state: Int) {
        when (state) {
            1 -> sender.cast<CommandSender>().sendMessage("§cUnknown or incomplete command, see below for error")
            2 -> sender.cast<CommandSender>().sendMessage("§cIncorrect argument for command")
            else -> return
        }
    }
}