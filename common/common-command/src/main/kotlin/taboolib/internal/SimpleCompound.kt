package taboolib.internal

import taboolib.common.Internal
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*

/**
 * TabooLib
 * taboolib.internal.SimpleCompound
 *
 * @author 坏黑
 * @since 2022/1/31 12:03 AM
 */
@Internal
class SimpleCompound : Component() {

    private var result = true

    private var incorrectSender: ActionIncorrect<*> = ActionIncorrect(ProxyCommandSender::class.java) { sender, _, _, _ ->
        sender.sendMessage("§cIncorrect sender for command")
    }

    private var incorrectCommand: ActionIncorrect<*> = ActionIncorrect(ProxyCommandSender::class.java) { _, context, index, state ->
        val args = context.args.toList().subListBy(0, index)
        var str = context.name
        if (args.size > 1) {
            str += " "
            str += args.subListBy(0, args.size - 1).joinToString(" ").trim()
        }
        if (str.length > 10) {
            str = "...${str.substring(str.length - 10, str.length)}"
        }
        if (args.isNotEmpty()) {
            str += " "
            str += "§c§n${args.last()}"
        }
        when (state) {
            1 -> context.sender.sendMessage("§cUnknown or incomplete command, see below for error")
            2 -> context.sender.sendMessage("§cIncorrect argument for command")
        }
        context.sender.sendMessage("§7$str§r§c§o<--[HERE]")
    }

    override fun createCompound(): Component {
        return SimpleCompound()
    }

    override fun execute(context: CommandContext<*>): Boolean {
        result = true
        // 空参数是一种特殊的状态，指的是玩家输入根命令且不附带任何参数，例如 [/test] 而不是 [/test ]
        if (context.args.isEmpty()) {
            val children = children(context)
            return if (children.isEmpty() || children.any { it.optional } || executor != null) {
                context.index = 0
                // 缺少 execute 代码块
                if (executor == null) {
                    (context.sender as ProxyCommandSender).sendMessage("§cEmpty command.")
                } else {
                    executor!!.exec(this, context, "")
                }
                result
            } else {
                incorrectCommand.exec(context, -1, 1)
                false
            }
        }
        fun process(cur: Int, component: Section): Boolean {
            val argument = context.args[cur]
            val children = component.children(context).firstOrNull {
                context.index = cur
                when (it) {
                    is SectionLiteral -> it.aliases.contains(argument)
                    is SectionDynamic -> {
                        val suggestion = it.suggestion
                        when {
                            argument.isEmpty() -> false
                            it.restrict?.exec(context, argument) == false -> false
                            suggestion?.uncheck == false && suggestion.exec(context)?.none { s -> s.equals(argument, true) } == true -> false
                            else -> true
                        }
                    }
                    else -> error("out of case")
                }
            }
            return if (children != null) {
                if (cur + 1 < context.args.size && children.children(context).isNotEmpty()) {
                    process(cur + 1, children)
                } else {
                    if (children.children(context).isEmpty() || children.children(context).any { it.optional } || children.executor != null) {
                        context.index = cur
                        // 缺少 execute 代码块
                        if (children.executor == null) {
                            (context.sender as ProxyCommandSender).sendMessage("§cEmpty command.")
                        } else {
                            children.executor!!.exec(this, context, context.args.joinBy(cur))
                        }
                        result
                    } else {
                        incorrectCommand.exec(context, cur + 1, 1)
                        false
                    }
                }
            } else {
                incorrectCommand.exec(context, cur + 1, 2)
                false
            }
        }
        return process(0, this)
    }

    override fun suggest(context: CommandContext<*>): List<String>? {
        // 空参数不需要触发补全机制
        if (context.args.isEmpty()) {
            return null
        }
        fun process(cur: Int, component: Section): List<String>? {
            val argument = context.args[cur]
            val children = component.children(context).firstOrNull {
                context.index = cur
                when (it) {
                    is SectionLiteral -> it.aliases.contains(argument)
                    is SectionDynamic -> {
                        val suggestion = it.suggestion
                        when {
                            argument.isEmpty() -> false
                            it.restrict?.exec(context, argument) == false -> false
                            suggestion?.uncheck == false && suggestion.exec(context)?.none { s -> s.equals(argument, true) } == true -> false
                            else -> true
                        }
                    }
                    else -> error("out of case")
                }
            }
            return when {
                children != null && cur + 1 < context.args.size -> {
                    process(cur + 1, children)
                }
                cur + 1 == context.args.size -> {
                    context.index = cur
                    val suggest = component.children(context).flatMap {
                        when (it) {
                            is SectionLiteral -> it.aliases.toList()
                            is SectionDynamic -> it.suggestion?.exec(context) ?: emptyList()
                            else -> emptyList()
                        }
                    }
                    suggest.filter { it.startsWith(argument, ignoreCase = true) }.ifEmpty { null }
                }
                else -> null
            }
        }
        return process(0, this)
    }

    override fun incorrectSender(function: (sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>) -> Unit) {
        incorrectSender = ActionIncorrect(ProxyCommandSender::class.java) { sender, context, _, _ -> function(sender, context) }
    }

    override fun incorrectCommand(function: (sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, index: Int, state: Int) -> Unit) {
        incorrectCommand = ActionIncorrect(ProxyCommandSender::class.java, function)
    }

    override fun sendIncorrectSender(context: CommandContext<*>) {
        incorrectSender.exec(context, 0, 0);
    }

    override fun sendIncorrectCommand(context: CommandContext<*>, index: Int, state: Int) {
        incorrectCommand.exec(context, index, state)
    }

    override fun setResult(value: Boolean) {
        result = value
    }

    fun Array<String>.joinBy(start: Int = 0, separator: String = " "): String {
        return filterIndexed { index, _ -> index >= start }.joinToString(separator)
    }

    fun <T> List<T>.subListBy(start: Int = 0, end: Int = size): List<T> {
        return filterIndexed { index, _ -> index in start until end }
    }
}