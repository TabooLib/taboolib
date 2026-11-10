package taboolib.platform

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.CommandRegistration
import com.hypixel.hytale.server.core.command.system.CommandSender
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.entity.entities.Player
import taboolib.common.Inject
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.service.PlatformCommand
import taboolib.common.util.unsafeLazy
import taboolib.platform.type.HytaleCommandSender
import taboolib.platform.type.HytalePlayer
import java.util.concurrent.ConcurrentHashMap
import taboolib.common.platform.command.component.CommandBase as TabooLibCommandBase

/**
 * TabooLib
 * taboolib.platform.HytaleCommand
 *
 * @author sky
 * @since 2024/1/1
 */
@Awake
@Inject
@PlatformSide(Platform.HYTALE)
class HytaleCommand : PlatformCommand {

    val plugin by unsafeLazy { HytalePlugin.getInstance() }

    /** 已注册的命令 -> CommandRegistration 映射，用于支持注销 */
    private val registeredCommands = ConcurrentHashMap<String, CommandRegistration>()

    override fun registerCommand(
        command: CommandStructure,
        executor: CommandExecutor,
        completer: CommandCompleter,
        commandBuilder: TabooLibCommandBase.() -> Unit,
    ) {
        // 创建 Hytale 命令
        val hytaleCommand = TabooLibHytaleCommand(
            command.name,
            command.description,
            executor,
            completer,
            command
        )
        
        // 注册命令并保存 registration
        val registration = plugin.commandRegistry.registerCommand(hytaleCommand)
        registeredCommands[command.name] = registration
        
        // 同时为别名也保存引用（它们共享同一个 registration）
        command.aliases.forEach { alias ->
            registeredCommands[alias] = registration
        }
    }

    override fun unregisterCommand(command: String) {
        val registration = registeredCommands.remove(command)
        if (registration != null) {
            registration.unregister()
            // 同时移除可能存在的别名引用
            registeredCommands.entries.removeIf { it.value === registration }
        }
    }

    override fun unregisterCommands() {
        // 使用 Set 去重，因为别名和主命令可能指向同一个 registration
        val uniqueRegistrations = registeredCommands.values.toSet()
        uniqueRegistrations.forEach { registration ->
            registration.unregister()
        }
        registeredCommands.clear()
    }

    override fun unknownCommand(sender: ProxyCommandSender, command: String, state: Int) {
        when (state) {
            1, 2 -> sender.sendMessage("Unknown command: $command")
        }
    }

    /**
     * TabooLib 命令适配器 - 将 TabooLib 命令转换为 Hytale 命令
     */
    class TabooLibHytaleCommand(
        name: String,
        description: String,
        private val executor: CommandExecutor,
        private val completer: CommandCompleter,
        private val structure: CommandStructure
    ) : CommandBase(name, description) {

        init {
            val permission = commandPermission(structure.permission)
            setAllowsExtraArguments(true)
            withRequiredArg("argument", "", ArgTypes.STRING).suggest { sender, input, _, result ->
                commandSuggestions(input) { args ->
                    completer.execute(adaptNativeCommandSender(sender), structure, structure.name, args)
                }.forEach { result.suggest(it) }
            }
            permission?.let { requirePermission(it) }
            addUsageVariant(object : CommandBase(description) {

                init {
                    permission?.let { requirePermission(it) }
                }

                override fun executeSync(context: CommandContext) {
                    executeCommand(context, emptyArray())
                }
            })
            if (structure.aliases.isNotEmpty()) {
                addAliases(*structure.aliases.toTypedArray())
            }
        }

        override fun executeSync(context: CommandContext) {
            executeCommand(context, commandArguments(context.inputString))
        }

        private fun executeCommand(context: CommandContext, args: Array<String>) {
            executor.execute(adaptNativeCommandSender(context.sender()), structure, structure.name, args)
        }
    }
}

private fun adaptNativeCommandSender(sender: CommandSender): ProxyCommandSender {
    return if (sender is Player) HytalePlayer(sender) else HytaleCommandSender(sender)
}

@JvmSynthetic
internal fun commandPermission(permission: String): String? {
    return permission.ifEmpty { null }
}

@JvmSynthetic
internal fun commandArguments(input: String): Array<String> {
    val parts = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return if (parts.size > 1) parts.drop(1).toTypedArray() else emptyArray()
}

@JvmSynthetic
internal fun commandSuggestions(input: String, completer: (Array<String>) -> List<String>?): List<String> {

    return completer(completionArguments(input)) ?: emptyList()
}

@JvmSynthetic
internal fun completionArguments(input: String): Array<String> {
    if (input.isEmpty()) {
        return arrayOf("")
    }
    val arguments = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.toMutableList()
    if (input.last().isWhitespace()) {
        arguments += ""
    }
    return arguments.toTypedArray()
}
