package taboolib.platform

import net.kyori.adventure.text.Component
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
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformCommand
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.util.unsafeLazy
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

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    val commandMap by unsafeLazy {
        Bukkit.getPluginManager().getProperty<SimpleCommandMap>("commandMap")!!
    }

    val knownCommands by unsafeLazy {
        commandMap.getProperty<MutableMap<String, Command>>("knownCommands")!!
    }

    val constructor: Constructor<PluginCommand> by unsafeLazy {
        PluginCommand::class.java.getDeclaredConstructor(String::class.java, Plugin::class.java).also {
            it.isAccessible = true
        }
    }

    val registeredCommands = ArrayList<CommandStructure>()

    private var isSupportedUnknownCommand = false

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
            pluginCommand.setProperty("usageMessage", command.usage)
            pluginCommand.setProperty("aliases", command.aliases)
            pluginCommand.setProperty("activeAliases", command.aliases)
            pluginCommand.setProperty("permission", permission)
            val permissionMessage = command.permissionMessage.ifEmpty { PlatformCommand.defaultPermissionMessage }
            try {
                // ClassCastException: Cannot cast java.lang.String to net.kyori.adventure.text.Component
                pluginCommand.setProperty("permissionMessage", permissionMessage)
            } catch (ex: ClassCastException) {
                pluginCommand.setProperty("permissionMessage", Component.text(permission))
            }
            // 注册权限
            fun registerPermission(permission: String, default: PermissionDefault) {
                if (Bukkit.getPluginManager().getPermission(permission) == null) {
                    try {
                        val p = Permission(permission, org.bukkit.permissions.PermissionDefault.values()[default.ordinal])
                        Bukkit.getPluginManager().addPermission(p)
                        Bukkit.getPluginManager().recalculatePermissionDefaults(p)
                        p.recalculatePermissibles()
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }
            registerPermission(permission, command.permissionDefault)
            command.permissionChildren.forEach {
                registerPermission(it.key, it.value)
            }
            // 注册命令
            knownCommands.remove(command.name)
            knownCommands["${plugin.name.lowercase()}:${pluginCommand.name}"] = pluginCommand
            knownCommands[pluginCommand.name] = pluginCommand
            pluginCommand.aliases.forEach {
                knownCommands[it] = pluginCommand
            }
            pluginCommand.register(commandMap)
            // 1.8 patch
            kotlin.runCatching {
                if (pluginCommand.getProperty<Any>("timings") == null) {
                    val timingsManager = Class.forName("co.aikar.timings.TimingsManager")
                    pluginCommand.setProperty("timings", timingsManager.invokeMethod("getCommandTiming", plugin.name, pluginCommand, isStatic = true))
                }
            }
            sync()
            registeredCommands.add(command)
        }
    }

    override fun unregisterCommand(command: String) {
        knownCommands.remove(command)
        sync()
    }

    override fun unregisterCommands() {
        registeredCommands.forEach { taboolib.common.platform.function.unregisterCommand(it) }
        sync()
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

    override fun isSupportedUnknownCommand(): Boolean {
        return isSupportedUnknownCommand
    }

    fun sync() {
        // 1.13 sync commands
        kotlin.runCatching {
            Bukkit.getServer().invokeMethod<Void>("syncCommands")
            Bukkit.getOnlinePlayers().forEach { it.updateCommands() }
            isSupportedUnknownCommand = true
        }
    }
}