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
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.Inject
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformCommand
import taboolib.common.util.unsafeLazy
import java.lang.reflect.Constructor
import java.util.concurrent.CopyOnWriteArrayList

internal fun commandLabelMatches(name: String, aliases: List<String>, input: String, namespace: String): Boolean {
    val separator = input.indexOf(':')
    val label = if (separator >= 0) {
        if (!input.substring(0, separator).equals(namespace, ignoreCase = true)) {
            return false
        }
        input.substring(separator + 1)
    } else {
        input
    }
    return label.equals(name, ignoreCase = true) || aliases.any { it.equals(label, ignoreCase = true) }
}

internal fun <T : Any> removeMappingsByIdentity(commands: MutableMap<String, T>, target: T): Boolean {
    val keys = commands.filterValues { it === target }.keys.toList()
    keys.forEach(commands::remove)
    return keys.isNotEmpty()
}

/**
 * TabooLib
 * taboolib.platform.BukkitCommand
 *
 * @author sky
 * @since 2021/6/26 2:33 下午
 */
@Awake
@Inject
@PlatformSide(Platform.BUKKIT)
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

    /**
     * 已注册的命令结构。
     *
     * 写入始终在 [commandLock] 内完成，但该字段是公开的、外部读取不持锁，
     * 因此使用 [CopyOnWriteArrayList] 保证并发读取时不会看到撕裂的中间状态。
     */
    val registeredCommands = CopyOnWriteArrayList<CommandStructure>()

    private val commandLock = Any()
    private val registeredCommandBindings = CopyOnWriteArrayList<RegisteredCommand>()
    private var isSupportedUnknownCommand = false

    private data class RegisteredCommand(val structure: CommandStructure, val command: PluginCommand)

    override fun registerCommand(
        command: CommandStructure,
        executor: CommandExecutor,
        completer: CommandCompleter,
        commandBuilder: CommandBase.() -> Unit,
    ) {
        submit(now = true) {
            val pluginCommand = constructor.newInstance(command.name, plugin)
            pluginCommand.setExecutor { sender, _, label, args ->
                executor.execute(adaptCommandSender(sender), command, label, args)
            }
            pluginCommand.setTabCompleter { sender, _, label, args ->
                completer.execute(adaptCommandSender(sender), command, label, args) ?: emptyList()
            }
            val permission = command.permission.ifEmpty { permissionProvider(command) }
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
                pluginCommand.setProperty("permissionMessage", Component.text(permissionMessage))
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
            // 1.8 patch
            runCatching {
                if (pluginCommand.getProperty<Any>("timings") == null) {
                    val timingsManager = Class.forName("co.aikar.timings.TimingsManager")
                    pluginCommand.setProperty("timings", timingsManager.invokeMethod("getCommandTiming", plugin.name, pluginCommand, isStatic = true))
                }
            }
            // 注册命令及身份记录作为同一个事务；同名重注册前先清理旧实例的全部映射
            synchronized(commandLock) {
                registeredCommandBindings
                    .filter { it.structure.name.equals(command.name, ignoreCase = true) }
                    .toList()
                    .forEach(::unregisterBinding)
                knownCommands["${plugin.name.lowercase()}:${pluginCommand.name}"] = pluginCommand
                knownCommands[pluginCommand.name] = pluginCommand
                pluginCommand.aliases.forEach {
                    knownCommands[it] = pluginCommand
                }
                pluginCommand.register(commandMap)
                registeredCommands.add(command)
                registeredCommandBindings.add(RegisteredCommand(command, pluginCommand))
            }
            sync()
        }
    }

    override fun unregisterCommand(command: String) {
        val removed = synchronized(commandLock) {
            registeredCommandBindings
                .filter { commandLabelMatches(it.structure.name, it.structure.aliases, command, plugin.name.lowercase()) }
                .toList()
                .also { it.forEach(::unregisterBinding) }
                .isNotEmpty()
        }
        if (removed) {
            sync()
        }
    }

    override fun unregisterCommands() {
        val removed = synchronized(commandLock) {
            registeredCommandBindings.toList().also { it.forEach(::unregisterBinding) }.isNotEmpty()
        }
        if (removed) {
            sync()
        }
    }

    private fun unregisterBinding(binding: RegisteredCommand) {
        removeMappingsByIdentity(knownCommands, binding.command)
        binding.command.unregister(commandMap)
        registeredCommandBindings.remove(binding)
        // 按身份而非等值移除：CommandStructure 可能存在等值但不同源的实例
        registeredCommands.removeIf { it === binding.structure }
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
        components += TextComponent(command).also {
            it.color = ChatColor.GRAY
        }
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
        runCatching {
            Bukkit.getServer().invokeMethod<Void>("syncCommands")
            Bukkit.getOnlinePlayers().forEach { it.invokeMethod<Void>("updateCommands") }
            isSupportedUnknownCommand = true
        }
    }

    companion object {

        var permissionProvider: (command: CommandStructure) -> String = { command ->
            "${BukkitPlugin.getInstance().name.lowercase()}.command.${command.name}.use"
        }
    }
}