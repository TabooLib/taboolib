package taboolib.common.platform.command.component

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.service.PlatformCommand
import taboolib.common.util.subList
import taboolib.common.util.t
import java.util.ArrayDeque

@Suppress("DuplicatedCode")
class CommandBase : CommandComponent(-1, false) {

    private val resultStack = ThreadLocal.withInitial { ArrayDeque<Boolean>() }

    internal var commandIncorrectSender: CommandUnknownNotify<*> =
        CommandUnknownNotify(ProxyCommandSender::class.java) { sender, _, _, _ ->
            sender.sendMessage(
                """
                    §c不匹配的命令发送者类型。
                    §cIncorrect sender for command.
                """.t()
            )
        }

    internal var commandIncorrectCommand: CommandUnknownNotify<*> =
        CommandUnknownNotify(ProxyCommandSender::class.java) { _, context, index, state ->
            val args = subList(context.realArgs.toList(), 0, index)
            var str = context.name
            if (args.size > 1) {
                str += " "
                str += subList(args, 0, args.size - 1).joinToString(" ").trim()
            }
            if (str.length > 10) {
                str = "...${str.substring(str.length - 10, str.length)}"
            }
            if (args.isNotEmpty()) {
                str += " "
                str += "§c§n${args.last()}"
            }
            val command = PlatformFactory.getService<PlatformCommand>()
            if (command.isSupportedUnknownCommand()) {
                command.unknownCommand(context.sender(), str, state)
            } else {
                when (state) {
                    1 -> context.sender().sendMessage(
                        """
                            §c未知或不完整的命令，错误见下
                            §cUnknown or incomplete command, see below for error
                        """.t()
                    )
                    2 -> context.sender().sendMessage(
                        """
                            §c命令参数错误
                            §cIncorrect argument for command
                        """.t()
                    )
                }
                context.sender().sendMessage(
                    """
                        §7$str§r§c§o<--[这里]
                        §7$str§r§c§o<--[HERE]
                    """.t()
                )
            }
        }

    fun execute(context: CommandContext<*>): Boolean {
        val results = resultStack.get()
        results.addLast(true)
        return try {
            executeInternal(context)
        } finally {
            results.removeLast()
            if (results.isEmpty()) {
                resultStack.remove()
            }
        }
    }

    private fun executeInternal(context: CommandContext<*>): Boolean {
        // 空参数是一种特殊的状态，指的是玩家输入根命令且不附带任何参数，例如 [/test] 而不是 [/test ]
        if (context.realArgs.isEmpty()) {
            // 获取下级节点
            val children = findChildren(context)
            // 下级节点为空 || 下级节点存在可选（optional）|| 当前节点存在执行器
            return if (children.isEmpty() || children.any { it.optional } || commandExecutor != null) {
                context.index = 0
                // 缺少 execute 代码块
                if (commandExecutor == null) {
                    context.sender().sendMessage(
                        """
                            §c空命令（无执行器）。
                            §cEmpty command (no executor).
                        """.t()
                    )
                } else {
                    commandExecutor!!.exec(this, context, "")
                }
                currentResult()
            } else {
                commandIncorrectCommand.exec(context, -1, 1)
                false
            }
        }
        fun process(cur: Int, component: CommandComponent): Boolean {
            // 更新参数内容
            context.index = cur
            context.currentComponent = component
            // 检索节点
            val find = component.findChildren(context, context.realArgs[cur])
            return if (find != null) {
                // 获取下级节点
                val children = find.findChildren(context)
                // 存在下级输入参数 && 下级节点有效
                if (cur + 1 < context.realArgs.size && children.isNotEmpty()) {
                    process(cur + 1, find)
                } else {
                    // 下级节点为空 || 下级节点存在可选（optional）|| 当前节点存在执行器
                    if (children.isEmpty() || children.any { it.optional } || find.commandExecutor != null) {
                        context.currentComponent = find
                        // 缺少 execute 代码块
                        if (find.commandExecutor == null) {
                            context.sender().sendMessage(
                                """
                                    §c空命令（无执行器）。
                                    §cEmpty command (no executor).
                                """.t()
                            )
                        } else {
                            find.commandExecutor!!.exec(this, context, context.self())
                        }
                        currentResult()
                    } else {
                        commandIncorrectCommand.exec(context, cur + 1, 1)
                        false
                    }
                }
            } else {
                commandIncorrectCommand.exec(context, cur + 1, 2)
                false
            }
        }
        return process(0, this)
    }

    fun suggest(context: CommandContext<*>): List<String>? {
        // 空参数不需要触发补全机制
        if (context.realArgs.isEmpty()) {
            return null
        }
        fun process(cur: Int, component: CommandComponent): List<String>? {
            context.index = cur
            context.currentComponent = component
            // 获取当前输入参数
            val current = context.realArgs[cur]
            // 检索节点
            val find = component.findChildren(context, current)
            if (find != null) {
                context.currentComponent = find
            }
            return when {
                find != null && cur + 1 < context.realArgs.size -> {
                    process(cur + 1, find)
                }
                cur + 1 == context.realArgs.size -> {
                    val suggest = component.findChildren(context).flatMap {
                        when (it) {
                            is CommandComponentLiteral -> if (it.hidden) emptyList() else it.aliases.toList()
                            is CommandComponentDynamic -> it.commandSuggestion?.exec(context) ?: emptyList()
                            else -> emptyList()
                        }
                    }
                    suggest.filter { current.isEmpty() || it.contains(current, ignoreCase = true) }.ifEmpty { null }
                }
                else -> null
            }
        }
        return process(0, this)
    }

    fun incorrectSender(function: (sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>) -> Unit) {
        this.commandIncorrectSender = CommandUnknownNotify(ProxyCommandSender::class.java) { sender, context, _, _ -> function(sender, context) }
    }

    fun incorrectCommand(function: (sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, index: Int, state: Int) -> Unit) {
        this.commandIncorrectCommand = CommandUnknownNotify(ProxyCommandSender::class.java, function)
    }

    private fun currentResult(): Boolean {
        return resultStack.get().peekLast() ?: true
    }

    fun setResult(value: Boolean) {
        val results = resultStack.get()
        if (results.isEmpty()) {
            resultStack.remove()
            return
        }
        results.removeLast()
        results.addLast(value)
    }
}
