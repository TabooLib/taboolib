package taboolib.platform

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.PluginCommand
import org.bukkit.command.SimpleCommandMap
import org.bukkit.permissions.Permission
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.*
import taboolib.common.reflect.Reflex.Companion.reflex
import java.lang.reflect.Constructor

/**
 * TabooLib
 * taboolib.platform.BukkitCommand
 *
 * @author sky
 * @since 2021/6/26 2:33 下午
 */
@Awake
@PlatformSide([Platform.BUKKIT])
class BukkitCommand : PlatformCommand {

    val plugin by lazy {
        JavaPlugin.getProvidingPlugin(BukkitIO::class.java) as BukkitPlugin
    }

    val commandMap by lazy {
        Bukkit.getPluginManager().reflex<SimpleCommandMap>("commandMap")!!
    }

    val knownCommands by lazy {
        commandMap.reflex<MutableMap<String, Command>>("knownCommands")!!
    }

    val constructor: Constructor<PluginCommand> by lazy {
        PluginCommand::class.java.getDeclaredConstructor(String::class.java, Plugin::class.java).also {
            it.isAccessible = true
        }
    }

    val registeredCommands = ArrayList<CommandStructure>()

    override fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter) {
        submit(now = true) {
            val pluginCommand = constructor.newInstance(command.name, plugin)
            pluginCommand.executor = org.bukkit.command.CommandExecutor { sender, _, label, args ->
                executor.execute(adaptCommandSender(sender), command, label, args)
            }
            pluginCommand.tabCompleter = org.bukkit.command.TabCompleter { sender, _, label, args ->
                completer.execute(adaptCommandSender(sender), command, label, args)
            }
            var permission = command.permission
            if (permission.isEmpty()) {
                permission = plugin.name.toLowerCase() + ".command.use"
            }
            // 修改属性
            pluginCommand.reflex("description", command.description)
            pluginCommand.reflex("usageMessage", command.usage)
            pluginCommand.reflex("aliases", command.aliases)
            pluginCommand.reflex("activeAliases", command.aliases)
            pluginCommand.reflex("permission", permission)
            pluginCommand.reflex("permissionMessage", command.permissionMessage)
            // 注册权限
            if (command.permissionDefault == PermissionDefault.TRUE || command.permissionDefault == PermissionDefault.NOT_OP) {
                if (Bukkit.getPluginManager().getPermission(permission) != null) {
                    try {
                        val p = Permission(permission, org.bukkit.permissions.PermissionDefault.values()[command.permissionDefault.ordinal])
                        Bukkit.getPluginManager().addPermission(p)
                        Bukkit.getPluginManager().recalculatePermissionDefaults(p)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }
            // 注册命令
            knownCommands.remove(command.name)
            commandMap.register(plugin.name, pluginCommand)
            registeredCommands.add(command)
        }
    }

    override fun unregisterCommand(command: String) {
        knownCommands.remove(command)
    }

    override fun unregisterCommands() {
        registeredCommands.forEach { unregisterCommand(it) }
    }
}