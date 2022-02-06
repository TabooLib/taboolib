package taboolib.platform

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.PluginCommand
import org.bukkit.command.SimpleCommandMap
import org.bukkit.permissions.Permission
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeConstructor
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.internal.Internal
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.*
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.service.PlatformCommand

/**
 * TabooLib
 * taboolib.platform.BukkitCommand
 *
 * @author sky
 * @since 2021/6/26 2:33 下午
 */
@Internal
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

    val registeredCommands = ArrayList<CommandInfo>()

    override fun registerCommand(command: CommandInfo, executor: CommandExecutor, completer: CommandCompleter, component: Component.() -> Unit) {
        TabooLib.booster().join(LifeCycle.ENABLE) {
            val pluginCommand = PluginCommand::class.java.invokeConstructor(command.name, plugin)
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
                pluginCommand.setProperty("permissionMessage", permissionMessage)
            } catch (ex: ClassCastException) { // ClassCastException: Cannot cast java.lang.String to net.kyori.adventure.text.Component
                pluginCommand.setProperty("permissionMessage", net.kyori.adventure.text.Component.text(permission))
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
            syncCommands()
            registeredCommands.add(command)
        }
    }

    override fun unregisterCommand(command: String) {
        knownCommands.remove(command)
        syncCommands()
    }

    override fun unregisterCommands() {
        registeredCommands.forEach { unregisterCommand(it.name) }
        syncCommands()
    }

    fun syncCommands() {
        // 1.13 sync commands
        kotlin.runCatching {
            Bukkit.getServer().invokeMethod<Void>("syncCommands")
            Bukkit.getOnlinePlayers().forEach { it.updateCommands() }
        }
    }
}