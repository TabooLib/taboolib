package taboolib.platform

import cn.nukkit.Server
import cn.nukkit.command.Command
import cn.nukkit.command.PluginCommand
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

    override fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter) {
        val registeredCommands = Server.getInstance().commandRegistry.reflex<MutableMap<String, Command>>("registeredCommands")
        val name = arrayListOf(command.name)
        name.addAll(command.aliases)
        name.forEach {
            val pluginCommand = PluginCommand(NukkitPlugin.getInstance(), CommandData.builder(it)
                .setDescription(command.description)
                .setUsageMessage(command.usage)
                .addPermission(command.permission)
                .setPermissionMessage(command.permissionMessage)
                .build())
        }
    }

    override fun unregisterCommand(command: String) {
        TODO("Not yet implemented")
    }

    override fun unregisterCommands() {
        TODO("Not yet implemented")
    }
}