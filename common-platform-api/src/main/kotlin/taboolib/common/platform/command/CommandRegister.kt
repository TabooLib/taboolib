package taboolib.common.platform.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.function.registerCommand

internal data class CommandHandlers(val executor: CommandExecutor, val completer: CommandCompleter)

/**
 * 构建命令的执行器与补全器。
 *
 * **注意**：命令树在注册时构建一次并全程复用，不再于每次执行 / 每次 Tab 补全时重建。
 * 因此 `literal(*运行时列表)` 这类在构建期读取可变状态的写法，
 * 在配置热重载后不会自动反映新值（此前依赖「每次重建」而能生效）。
 * 需要动态内容请改用 `dynamic { suggestion { ... } }`，其回调在每次补全时执行。
 */
internal fun createCommandHandlers(newParser: Boolean, commandBuilder: CommandBase.() -> Unit): CommandHandlers {
    val commandBase = CommandBase().also(commandBuilder)
    return CommandHandlers(
        executor = object : CommandExecutor {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean {
                return commandBase.execute(CommandContext(sender, command, name, commandBase, newParser, args))
            }
        },
        completer = object : CommandCompleter {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                return commandBase.suggest(CommandContext(sender, command, name, commandBase, newParser, args))
            }
        }
    )
}

/**
 * 注册一个命令
 *
 * @param name 命令名
 * @param aliases 命令别名
 * @param description 命令描述
 * @param usage 命令用法
 * @param permission 命令权限
 * @param permissionMessage 命令权限提示
 * @param permissionDefault 命令权限默认值
 * @param permissionChildren 命令权限子节点
 * @param commandBuilder 命令构建器
 */
fun command(
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.OP,
    permissionChildren: Map<String, PermissionDefault> = emptyMap(),
    newParser: Boolean = false,
    commandBuilder: CommandBase.() -> Unit,
) {
    val handlers = createCommandHandlers(newParser, commandBuilder)
    registerCommand(
        // 创建命令结构
        CommandStructure(name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren, newParser),
        // 复用注册阶段构建的命令树
        handlers.executor,
        handlers.completer,
        // 传入原始命令构建器
        commandBuilder
    )
}

/**
 * 注册一个简易命令
 *
 * @param name 命令名
 * @param aliases 命令别名
 * @param description 命令描述
 * @param usage 命令用法
 * @param permission 命令权限
 * @param permissionMessage 命令权限提示
 * @param permissionDefault 命令权限默认值
 * @param permissionChildren 命令权限子节点
 * @param executor 命令构建器
 */
fun simpleCommand(
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.OP,
    permissionChildren: Map<String, PermissionDefault> = emptyMap(),
    completer: CommandCompleter? = null,
    executor: (sender: ProxyCommandSender, args: Array<String>) -> Unit,
) {
    registerCommand(
        // 创建命令结构
        CommandStructure(name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren, false),
        // 创建执行器
        object : CommandExecutor {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean {
                executor(sender, args)
                return true
            }
        },
        // 创建补全器
        completer ?: object : CommandCompleter {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): List<String> {
                return emptyList()
            }
        },
    ) {}
}