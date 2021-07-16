package taboolib.common.platform

import taboolib.common.Isolated
import taboolib.common.util.subList

/**
 * TabooLib
 * taboolib.module.command.Command
 *
 * @author sky
 * @since 2021/6/25 12:50 上午
 */
@Isolated
object CommandBuilder {

    class CommandBase : CommandComponent(false) {

        internal var unknownNotify = CommandUnknownNotify { context, index ->
            // commands.help.failed
            context.sender.sendMessage("§cUnknown or incomplete command, see below for error")
            var str = "/${context.name}"
            if (index > 0) {
                str += " "
                str += subList(context.args.toList(), 0, index + 1).joinToString(" ").trim()
            }
            if (str.length > 10) {
                str = "...${str.substring(str.length - 10, str.length)}"
            }
            // command.context.here
            context.sender.sendMessage("§7$str§c<--[HERE]")
        }

        fun execute(context: CommandContext): Boolean {
            // 空参数是一种特殊的状态，指的是玩家输入根命令且不附带任何参数，例如 [/test] 而不是 [/test ]
            if (context.args.isEmpty()) {
                return if (children.any { it.optional }) {
                    executor?.function?.invoke(context, "")
                    true
                } else {
                    unknownNotify.function.invoke(context, 0)
                    false
                }
            }
            fun process(cur: Int, component: CommandComponent): Boolean {
                val argument = context.args[cur]
                val children = component.children.firstOrNull {
                    when (it) {
                        is CommandComponentLiteral -> it.alias.contains(argument)
                        is CommandComponentDynamic -> argument.isNotEmpty() && it.restrict.function.invoke(context, argument)
                        else -> error("out of case")
                    }
                }
                return if (children != null) {
                    if (cur + 1 < context.args.size) {
                        process(cur + 1, children)
                    } else {
                        if (children.children.isEmpty() || children.children.any { it.optional }) {
                            children.executor?.function?.invoke(context, argument)
                            true
                        } else {
                            unknownNotify.function.invoke(context, cur)
                            false
                        }
                    }
                } else {
                    unknownNotify.function.invoke(context, cur)
                    false
                }
            }
            return process(0, this)
        }

        fun suggest(context: CommandContext): List<String>? {
            // 空参数不需要触发补全机制
            if (context.args.isEmpty()) {
                return null
            }
//            fun process(cur: Int, component: CommandComponent): List<String>? {
//                val argument = context.args[cur]
//                val children = component.children.firstOrNull {
//                    when (it) {
//                        is CommandComponentLiteral -> it.alias.contains(argument)
//                        is CommandComponentDynamic -> argument.isNotEmpty() && it.restrict.function.invoke(context, argument)
//                        else -> error("out of case")
//                    }
//                }
//                return if (children != null) {
//                    if (cur + 1 < context.args.size) {
//                        process(cur + 1, children)
//                    } else {
//                        val suggest = children.children.flatMap {
//                            when (it) {
//                                is CommandComponentLiteral -> it.alias.toList()
//                                is CommandComponentDynamic -> it.suggestion?.function?.invoke(context) ?: emptyList()
//                                else -> emptyList()
//                            }
//                        }
//                        suggest.filter { it.startsWith(argument, ignoreCase = true) }.ifEmpty { null }
//                    }
//                } else {
//                    val suggest = component.children.flatMap {
//                        when (it) {
//                            is CommandComponentLiteral -> it.alias.toList()
//                            is CommandComponentDynamic -> it.suggestion?.function?.invoke(context) ?: emptyList()
//                            else -> emptyList()
//                        }
//                    }
//                    suggest.filter { it.startsWith(argument, ignoreCase = true) }.ifEmpty { null }
//                }
//            }
//            return process(0, this)
        }

        fun unknown(function: (context: CommandContext, index: Int) -> Unit) {
            this.unknownNotify = CommandUnknownNotify(function)
        }

        override fun toString(): String {
            return "CommandBase(unknownNotify=$unknownNotify) ${super.toString()}"
        }
    }

    class CommandExecutor(val function: (context: CommandContext, argument: String) -> Unit)

    class CommandRestrict(val function: (context: CommandContext, argument: String) -> Boolean)

    class CommandSuggestion(val function: (context: CommandContext) -> List<String>?)

    class CommandUnknownNotify(val function: (context: CommandContext, index: Int) -> Unit)

    class CommandComponentLiteral(vararg val alias: String, optional: Boolean) : CommandComponent(optional)

    class CommandComponentDynamic(optional: Boolean) : CommandComponent(optional) {

        internal var suggestion: CommandSuggestion? = null
        internal var restrict = CommandRestrict { _, _ -> true }

        fun suggestion(function: (context: CommandContext) -> List<String>?) {
            this.suggestion = CommandSuggestion(function)
        }

        fun restrict(function: (context: CommandContext, argument: String) -> Boolean) {
            this.restrict = CommandRestrict(function)
        }

        override fun toString(): String {
            return "CommandComponentDynamic(suggestion=$suggestion, restrict=$restrict) ${super.toString()}"
        }
    }

    abstract class CommandComponent(val optional: Boolean) {

        internal var executor: CommandExecutor? = null
        internal val children = ArrayList<CommandComponent>()

        fun literal(vararg alias: String, optional: Boolean = false, literal: CommandComponentLiteral.() -> Unit) {
            children += CommandComponentLiteral(*alias, optional = optional).also(literal)
        }

        fun dynamic(optional: Boolean = false, dynamic: CommandComponentDynamic.() -> Unit) {
            children += CommandComponentDynamic(optional).also(dynamic)
        }

        fun execute(function: (context: CommandContext, argument: String) -> Unit) {
            this.executor = CommandExecutor(function)
        }

        override fun toString(): String {
            return "CommandComponent(optional=$optional, executor=$executor, children=$children)"
        }
    }
}