package taboolib.platform

import cn.nukkit.Server
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandData
import taboolib.common.platform.*
import taboolib.common.reflect.Reflex.Companion.reflex

/**
 * TabooLib
 * taboolib.platform.NukkitCommand
 *
 * @author sky
 * @since 2021/7/3 1:15 上午
 */
@PlatformSide([Platform.NUKKIT])
class NukkitCommand : PlatformCommand {

    val structure = ArrayList<CommandStructure>()

    val registeredCommands by lazy {
        Server.getInstance().commandRegistry.reflex<MutableMap<String, Command>>("registeredCommands")!!
    }

    override fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter) {
        val registerCommand = object : Command(command.name, CommandData.builder(command.name)
            .setDescription(command.description)
            .setUsageMessage(command.usage)
            .addPermission(command.permission)
            .setPermissionMessage(command.permissionMessage)
            .build()) {

            override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
                return executor.execute(adaptCommandSender(sender), command, commandLabel, args)
            }
        }
        structure += command
        registeredCommands[command.name] = registerCommand
        command.aliases.forEach { registeredCommands[it] = registerCommand }
    }

    override fun unregisterCommand(command: String) {
        TODO("Not yet implemented")
    }

    override fun unregisterCommands() {
        TODO("Not yet implemented")
    }
}