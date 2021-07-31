package taboolib.platform

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.TranslatableComponent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.command.SimpleCommandMap
import org.bukkit.permissions.Permission
import org.bukkit.plugin.Plugin
import taboolib.common.platform.*
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.setProperty
import java.lang.reflect.Constructor
import java.util.*

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

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    val commandMap by lazy {
        Bukkit.getPluginManager().getProperty<SimpleCommandMap>("commandMap")!!
    }

    val knownCommands by lazy {
        commandMap.getProperty<MutableMap<String, Command>>("knownCommands")!!
    }

    val constructor: Constructor<PluginCommand> by lazy {
        PluginCommand::class.java.getDeclaredConstructor(String::class.java, Plugin::class.java).also {
            it.isAccessible = true
        }
    }

    val registeredCommands = ArrayList<CommandStructure>()

    override fun registerCommand(
        command: CommandStructure,
        executor: CommandExecutor,
        completer: CommandCompleter,
        commandBuilder: CommandBuilder.CommandBase.() -> Unit,
    ) {
        submit(now = true) {
            val pluginCommand = constructor.newInstance(command.name, plugin)
            pluginCommand.setExecutor { sender, _, label, args ->
                executor.execute(adaptCommandSender(sender), command, label, args)
            }
            pluginCommand.setTabCompleter { sender, _, label, args ->
                completer.execute(adaptCommandSender(sender), command, label, args) ?: emptyList()
            }
            val permission = command.permission.ifEmpty { "${plugin.name.lowercase()}.command.use" }
            // 修改属性
            pluginCommand.setProperty("description", command.description.ifEmpty { command.name })
            pluginCommand.setProperty("usageMessage", command.usage.ifEmpty { command.name })
            pluginCommand.setProperty("aliases", command.aliases)
            pluginCommand.setProperty("activeAliases", command.aliases)
            pluginCommand.setProperty("permission", permission)
            pluginCommand.setProperty("permissionMessage", command.permissionMessage.ifEmpty { PlatformCommand.defaultPermissionMessage })
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
            knownCommands["$pluginId:${pluginCommand.name}"] = pluginCommand
            knownCommands[pluginCommand.name] = pluginCommand
            pluginCommand.aliases.forEach {
                knownCommands[it] = pluginCommand
            }
            pluginCommand.register(commandMap)
            registeredCommands.add(command)
        }
    }

    override fun unregisterCommand(command: String) {
        knownCommands.remove(command)
    }

    override fun unregisterCommands() {
        registeredCommands.forEach { unregisterCommand(it) }
    }

    override fun unknownCommand(sender: ProxyCommandSender, command: String, state: Int) {
        when (state) {
            1 -> sender.cast<CommandSender>().spigot().sendMessage(TranslatableComponent("command.unknown.command").also {
                it.color = ChatColor.RED
            })
            2 -> sender.cast<CommandSender>().spigot().sendMessage(TranslatableComponent("command.unknown.argument").also {
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
        sender.cast<CommandSender>().spigot().sendMessage(*components.toTypedArray())
    }
}