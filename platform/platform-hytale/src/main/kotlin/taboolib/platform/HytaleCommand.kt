package taboolib.platform

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.CommandRegistration
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import taboolib.common.Inject
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.service.PlatformCommand
import taboolib.common.util.unsafeLazy
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
            // 允许额外参数（TabooLib 自己处理参数解析）
            setAllowsExtraArguments(true)
            
            // 添加别名
            if (structure.aliases.isNotEmpty()) {
                addAliases(*structure.aliases.toTypedArray())
            }
        }

        override fun executeSync(context: CommandContext) {
            val sender = adaptCommandSender(context.sender())
            // 直接从输入字符串解析参数
            // inputString 格式: "commandName arg1 arg2 arg3"
            val inputString = context.inputString
            val args = if (inputString.isBlank()) {
                emptyArray()
            } else {
                // 移除命令名，只保留参数
                val parts = inputString.split(" ").filter { it.isNotBlank() }
                if (parts.size > 1) {
                    parts.drop(1).toTypedArray()
                } else {
                    emptyArray()
                }
            }
            
            executor.execute(sender, structure, structure.name, args)
        }
    }
}
