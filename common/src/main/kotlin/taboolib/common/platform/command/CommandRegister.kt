package taboolib.common.platform.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.function.registerCommand

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
    registerCommand(
        // 创建命令结构
        CommandStructure(name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren),
        // 创建执行器
        object : CommandExecutor {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean {
                val commandBase = CommandBase().also(commandBuilder)
                return commandBase.execute(CommandContext(sender, command, name, commandBase, newParser, args))
            }
        },
        // 创建补全器
        object : CommandCompleter {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                val commandBase = CommandBase().also(commandBuilder)
                return commandBase.suggest(CommandContext(sender, command, name, commandBase, newParser, args))
            }
        },
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
        CommandStructure(name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren),
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