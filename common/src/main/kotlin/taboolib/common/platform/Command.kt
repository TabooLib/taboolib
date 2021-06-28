package taboolib.common.platform

import taboolib.common.util.join

/**
 * TabooLib
 * taboolib.module.command.Command
 *
 * @author sky
 * @since 2021/6/25 12:50 上午
 */
abstract class Command(val sender: ProxyCommandSender, protected val successBox: CommandBox<Boolean>, protected val completeBox: CommandBox<List<String>?>) {

    protected val literals = HashMap<String, LiteralCommandBuilder>()
    protected var required: ArgumentCommandBuilder? = null
    protected var optional: ArgumentCommandBuilder? = null

    protected var complete: ((CommandContext) -> List<String>?)? = null
    protected var restrict: (String.(CommandContext) -> Boolean)? = null

    protected var lost: ((CommandContext) -> Unit) = {
        // commands.help.failed
        sender.sendMessage("§cUnknown or incomplete command, see below for error")
        var str = "/${it.name} ${it.args.joinToString(" ")}".trim()
        if (str.length > 10) {
            str = "...${str.substring(str.length - 10, str.length)}"
        }
        // command.context.here
        sender.sendMessage("§7$str§c<--[HERE]")
    }

    var success: Boolean
        get() = successBox.value
        set(value) {
            successBox.value = value
        }

    fun lost(lost: (CommandContext) -> Unit) {
        this.lost = lost
    }

    fun literal(name: String, literal: ArgumentCommand.(CommandContext) -> Unit) {
        this.literals[name] = LiteralCommandBuilder(literal, null)
    }

    fun required(required: ArgumentCommand.(CommandContext) -> Unit) {
        this.required = ArgumentCommandBuilder(required, null)
        this.optional = null
    }

    fun optional(optional: ArgumentCommand.(CommandContext) -> Unit) {
        this.optional = ArgumentCommandBuilder(optional, null)
        this.restrict = { isNotEmpty() }
        this.required = null
    }

    protected fun createCommand(
        sender: ProxyCommandSender,
        context: CommandContext,
        argument: String,
        executor: Executor,
        commandType: CommandType = CommandType.ALL
    ): Pair<ArgumentCommand, Executor> {
        val command = ArgumentCommand(sender, successBox, completeBox, this, argument, executor, commandType)
        return command to when (commandType) {
            CommandType.ALL -> {
                literals[argument]?.builder?.invoke(command, context)
                required?.builder?.invoke(command, context)
                optional?.builder?.invoke(command, context)
                executor
            }
            CommandType.LITERAL -> {
                literals[argument]!!.run {
                    if (this.executor == null) {
                        this.executor = executor
                    }
                    builder.invoke(command, context)
                    this.executor!!
                }
            }
            CommandType.REQUIRED -> {
                required!!.run {
                    if (this.executor == null) {
                        this.executor = executor
                    }
                    builder.invoke(command, context)
                    this.executor!!
                }
            }
            CommandType.OPTIONAL -> {
                optional!!.run {
                    if (this.executor == null) {
                        this.executor = executor
                    }
                    builder.invoke(command, context)
                    this.executor!!
                }
            }
        }
    }

    protected fun createComplete(context: CommandContext) {
        completeBox.value = literals.keys.toMutableList().also {
            it.addAll(complete?.invoke(context) ?: emptyList())
        }
    }

    protected fun isFinally(command: ArgumentCommand): Boolean {
        return command.literals.isEmpty() && command.required == null
    }

    protected fun run(sender: ProxyCommandSender, context: CommandContext, index: Int, inExecute: Boolean): Pair<String, Int> {
        // 空参数是一种特殊的状态，指的是玩家输入根命令且不附带任何参数，例如 [/test] 而不是 [/test ]
        val executor = Executor()
        val argument = context.args.getOrNull(index) ?: ""
        if (context.args.isEmpty()) {
            if (inExecute) {
                // 如果存在子结构则触发缺失方法
                if (literals.isNotEmpty() || required != null) {
                    lost.invoke(context)
                } else if (this is BaseCommand) {
                    this.executor(context, argument)
                }
            } else {
                createComplete(context)
            }
        }
        // 如果不存在子结构则在当前结构下执行逻辑
        else if (isFinally(createCommand(sender, context, argument, executor, CommandType.ALL).first)) {
            if (inExecute) {
                val commandType: CommandType
                val commandBuilder = when {
                    literals.containsKey(argument) -> {
                        commandType = CommandType.LITERAL
                        literals[argument]
                    }
                    required != null -> {
                        commandType = CommandType.REQUIRED
                        required
                    }
                    optional != null -> {
                        commandType = CommandType.OPTIONAL
                        optional
                    }
                    else -> {
                        commandType = CommandType.ALL
                        null
                    }
                }
                if (commandBuilder == null || (commandType.allowRestrict && restrict?.invoke(argument, context) == false)) {
                    lost.invoke(context)
                } else {
                    createCommand(sender, context, argument, commandBuilder.executor ?: executor, commandType).second(context, join(context.args, index))
                }
            } else {
                createComplete(context)
            }
        } else {
            // 向下递归的条件是符合参数及约束条件
            if (literals.containsKey(argument) && restrict?.invoke(argument, context) != false || optional != null) {
                return createCommand(sender, context, argument, executor).first.run(sender, context, index + 1, inExecute)
            } else {
                if (inExecute) {
                    lost.invoke(context)
                } else {
                    createComplete(context)
                }
            }
        }
        return argument to index
    }

    open class BaseCommand(sender: ProxyCommandSender, b1: CommandBox<Boolean>, b2: CommandBox<List<String>?>) :
        Command(sender, b1, b2) {

        val executor = Executor()

        open fun complete(sender: ProxyCommandSender, context: CommandContext): List<String>? {
            val result = run(sender, context, 0, false)
            if (result.second + 1 == context.args.size) {
                val completed = (if (completeBox.value?.isEmpty() == true) null else completeBox.value) ?: return null
                val similar = completed.filter { it.startsWith(result.first, ignoreCase = true) }
                return if (similar.isEmpty()) completed else similar
            } else {
                return null
            }
        }

        open fun execute(sender: ProxyCommandSender, context: CommandContext): Boolean {
            run(sender, context, 0, true)
            return success
        }

        open fun execute(func: (CommandContext) -> Unit) {
            executor.func = { func(it) }
        }
    }

    open class ArgumentCommand(
        sender: ProxyCommandSender,
        b1: CommandBox<Boolean>,
        b2: CommandBox<List<String>?>,
        val parent: Command,
        val argument: String,
        val executor: Executor,
        val commandType: CommandType
    ) :
        Command(sender, b1, b2) {

        open fun complete(func: (CommandContext) -> List<String>?) {
            parent.complete = func
        }

        open fun restrict(func: String.(CommandContext) -> Boolean) {
            parent.restrict = func
        }

        open fun execute(func: String.(CommandContext) -> Unit) {
            executor.func = func
        }
    }

    open class Executor(var func: (String.(CommandContext) -> Unit)? = null) {

        operator fun invoke(context: CommandContext, argument: String) {
            func?.invoke(argument, context)
        }
    }

    open class CommandBuilder(var executor: Executor?)

    open class LiteralCommandBuilder(val builder: ArgumentCommand.(CommandContext) -> Unit, executor: Executor?) : CommandBuilder(executor)

    open class ArgumentCommandBuilder(val builder: ArgumentCommand.(CommandContext) -> Unit, executor: Executor?) : CommandBuilder(executor)

    enum class CommandType(val allowRestrict: Boolean = false) {

        ALL, LITERAL, REQUIRED(true), OPTIONAL(true)
    }
}