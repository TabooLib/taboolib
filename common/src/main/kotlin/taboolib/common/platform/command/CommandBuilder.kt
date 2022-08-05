package taboolib.common.platform.command

import taboolib.common.Isolated
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.service.PlatformCommand
import taboolib.common.util.join
import taboolib.common.util.subList

/**
 * TabooLib
 * taboolib.module.command.Command
 *
 * @author sky
 * @since 2021/6/25 12:50 上午
 */
@Suppress("UNCHECKED_CAST", "DuplicatedCode")
@Isolated
object CommandBuilder {

    class CommandBase : CommandComponent(false) {

        internal var result = true

        var commandIncorrectSender: CommandUnknownNotify<*> = CommandUnknownNotify(ProxyCommandSender::class.java) { sender, _, _, _ ->
            sender.sendMessage("§cIncorrect sender for command")
        }

        var commandIncorrectCommand: CommandUnknownNotify<*> = CommandUnknownNotify(ProxyCommandSender::class.java) { _, context, index, state ->
            val args = subList(context.args.toList(), 0, index)
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
                command.unknownCommand(context.sender, str, state)
            } else {
                when (state) {
                    1 -> context.sender.sendMessage("§cUnknown or incomplete command, see below for error")
                    2 -> context.sender.sendMessage("§cIncorrect argument for command")
                }
                context.sender.sendMessage("§7$str§r§c§o<--[HERE]")
            }
        }

        fun execute(context: CommandContext<*>): Boolean {
            result = true
            // 空参数是一种特殊的状态，指的是玩家输入根命令且不附带任何参数，例如 [/test] 而不是 [/test ]
            if (context.args.isEmpty()) {
                val children = children(context)
                return if (children.isEmpty() || children.any { it.optional } || commandExecutor != null) {
                    context.index = 0
                    // 缺少 execute 代码块
                    if (commandExecutor == null) {
                        (context.sender as ProxyCommandSender).sendMessage("§cEmpty command.")
                    } else {
                        commandExecutor!!.exec(this, context, "")
                    }
                    result
                } else {
                    commandIncorrectCommand.exec(context, -1, 1)
                    false
                }
            }
            fun process(cur: Int, component: CommandComponent): Boolean {
                val argument = context.args[cur]
                val children = component.children(context).firstOrNull {
                    context.index = cur
                    when (it) {
                        is CommandComponentLiteral -> it.aliases.any { a -> a.equals(argument, true) }
                        is CommandComponentDynamic -> {
                            val suggestion = it.commandSuggestion
                            when {
                                argument.isEmpty() -> false
                                it.commandRestrict?.exec(context, argument) == false -> false
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
                        if (children.children(context).isEmpty() || children.children(context).any { it.optional } || children.commandExecutor != null) {
                            context.index = cur
                            // 缺少 execute 代码块
                            if (children.commandExecutor == null) {
                                (context.sender as ProxyCommandSender).sendMessage("§cEmpty command.")
                            } else {
                                children.commandExecutor!!.exec(this, context, join(context.args, cur))
                            }
                            result
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
            if (context.args.isEmpty()) {
                return null
            }
            fun process(cur: Int, component: CommandComponent): List<String>? {
                val argument = context.args[cur]
                val children = component.children(context).firstOrNull {
                    context.index = cur
                    when (it) {
                        is CommandComponentLiteral -> it.aliases.any { a -> a.equals(argument, true) }
                        is CommandComponentDynamic -> {
                            val suggestion = it.commandSuggestion
                            when {
                                argument.isEmpty() -> false
                                it.commandRestrict?.exec(context, argument) == false -> false
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
                                is CommandComponentLiteral -> it.aliases.toList()
                                is CommandComponentDynamic -> it.commandSuggestion?.exec(context) ?: emptyList()
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

        fun incorrectSender(function: (sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>) -> Unit) {
            this.commandIncorrectSender = CommandUnknownNotify(ProxyCommandSender::class.java) { sender, context, _, _ -> function(sender, context) }
        }

        fun incorrectCommand(function: (sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, index: Int, state: Int) -> Unit) {
            this.commandIncorrectCommand = CommandUnknownNotify(ProxyCommandSender::class.java, function)
        }

        fun setResult(value: Boolean) {
            result = value
        }
    }

    abstract class CommandBinder<T>(val bind: Class<T>) {

        fun cast(context: CommandContext<*>): T? {
            val sender = context.sender as ProxyCommandSender
            return when {
                bind.isInstance(sender) -> context.sender as? T
                bind.isInstance(sender.origin) -> context.sender.origin as T
                else -> null
            }
        }
    }

    class CommandExecutor<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, argument: String) -> Unit) : CommandBinder<T>(bind) {

        fun exec(commandBase: CommandBase, context: CommandContext<*>, argument: String) {
            val sender = cast(context)
            if (sender != null) {
                function.invoke(sender, CommandContext(sender, context.command, context.name, context.commandCompound, context.args, context.index), argument)
            } else {
                commandBase.commandIncorrectSender.exec(context, 0, 0)
            }
        }
    }

    class CommandRestrict<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, argument: String) -> Boolean) : CommandBinder<T>(bind) {

        fun exec(context: CommandContext<*>, argument: String): Boolean? {
            val sender = cast(context)
            return if (sender != null) {
                function.invoke(sender, CommandContext(sender, context.command, context.name, context.commandCompound, context.args, context.index), argument)
            } else {
                null
            }
        }
    }

    class CommandSuggestion<T>(bind: Class<T>, val uncheck: Boolean, val function: (sender: T, context: CommandContext<T>) -> List<String>?) :
        CommandBinder<T>(bind) {

        fun exec(context: CommandContext<*>): List<String>? {
            val sender = cast(context)
            return if (sender != null) {
                function.invoke(sender, CommandContext(sender, context.command, context.name, context.commandCompound, context.args, context.index))
            } else {
                null
            }
        }
    }

    class CommandUnknownNotify<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, index: Int, state: Int) -> Unit) :
        CommandBinder<T>(bind) {

        fun exec(context: CommandContext<*>, index: Int, state: Int) {
            val sender = cast(context)
            if (sender != null) {
                function.invoke(sender,
                    CommandContext(sender, context.command, context.name, context.commandCompound, context.args, context.index),
                    index,
                    state)
            }
        }
    }

    class CommandComponentLiteral(vararg val aliases: String, optional: Boolean, permission: String) : CommandComponent(optional, permission)

    class CommandComponentDynamic(val commit: String, optional: Boolean, permission: String) : CommandComponent(optional, permission) {

        var commandSuggestion: CommandSuggestion<*>? = null
        var commandRestrict: CommandRestrict<*>? = null

        inline fun <reified T> suggestion(uncheck: Boolean = false, noinline function: (sender: T, context: CommandContext<T>) -> List<String>?) {
            this.commandSuggestion = CommandSuggestion(T::class.java, uncheck, function)
        }

        inline fun <reified T> restrict(noinline function: (sender: T, context: CommandContext<T>, argument: String) -> Boolean) {
            this.commandRestrict = CommandRestrict(T::class.java, function)
        }
    }

    abstract class CommandComponent(val optional: Boolean, val permission: String = "") {

        var commandExecutor: CommandExecutor<*>? = null
        val children = ArrayList<CommandComponent>()

        fun children(context: CommandContext<*>): List<CommandComponent> {
            return children.filter { it.permission.isEmpty() || context.checkPermission(it.permission) }
        }

        fun literal(vararg aliases: String, optional: Boolean = false, permission: String = "", literal: CommandComponentLiteral.() -> Unit) {
            children += CommandComponentLiteral(*aliases, optional = optional, permission = permission).also(literal)
        }

        fun dynamic(commit: String = "...", repeat: Int = 1, optional: Boolean = false, permission: String = "", dynamic: CommandComponentDynamic.() -> Unit) {
            when {
                repeat < 1 -> {
                    error("repeat must > 0")
                }
                repeat == 1 -> {
                    children += CommandComponentDynamic(commit, optional, permission).also(dynamic)
                }
                else -> {
                    TODO("unsupported")
                }
            }
        }

        inline fun <reified T> execute(noinline function: (sender: T, context: CommandContext<T>, argument: String) -> Unit) {
            this.commandExecutor = CommandExecutor(T::class.java, function)
        }
    }
}
