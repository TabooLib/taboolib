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

    protected val literals = HashMap<String, Command.(CommandContext) -> Unit>()
    protected var required: (ArgumentCommand.(CommandContext) -> Unit)? = null
    protected var optional: (ArgumentCommand.(CommandContext) -> Unit)? = null

    protected var execute: (String.(CommandContext) -> Unit)? = null
    protected var complete: ((CommandContext) -> List<String>?)? = null
    protected var restrict: (String.(CommandContext) -> Boolean)? = null

    protected var lost: ((CommandContext) -> Unit) = {
        // commands.help.failed
        sender.sendMessage("§cUnknown or incomplete command, see below for error")
        var str = it.args.joinToString(" ")
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

    fun literal(name: String, literal: Command.(CommandContext) -> Unit) {
        literals[name] = literal
    }

    fun required(required: ArgumentCommand.(CommandContext) -> Unit) {
        this.required = required
        optional = null
    }

    fun optional(optional: ArgumentCommand.(CommandContext) -> Unit) {
        this.optional = optional
        required = null
    }

    protected fun end(): Boolean {
        return literals.isEmpty() && required == null && optional == null
    }

    protected fun run(sender: ProxyCommandSender, context: CommandContext, index: Int, inExecute: Boolean) {
        // 检查参数
        if (literals.isNotEmpty() || required != null) {
            // 输入参数数量小于约定参数数量
            if (context.args.size < index + 1) {
                if (inExecute) {
                    lost.invoke(context)
                }
            } else if (end()) {
                if (inExecute) {
                    execute?.invoke(join(context.args, index), context)
                } else {
                    completeBox.value = complete?.invoke(context) ?: literals.keys.toList()
                }
            } else {
                val command = ArgumentCommand(sender, successBox, completeBox, context.args[index])
                val sub = literals[context.args[index]] ?: required!!
                sub.invoke(command, context)
                command.run(sender, context, index + 1, inExecute)
            }
        }
        // 存在可选参数
        else if (optional != null && index < context.args.size) {
            if (end()) {
                if (inExecute) {
                    execute?.invoke(join(context.args, index), context)
                } else {
                    completeBox.value = complete?.invoke(context)
                }
            } else {
                val command = ArgumentCommand(sender, successBox, completeBox, context.args[index])
                optional!!.invoke(command, context)
                command.run(sender, context, index + 1, inExecute)
            }
        } else {
            if (inExecute) {
                execute?.invoke(join(context.args, index), context)
            } else {
                completeBox.value = complete?.invoke(context)
            }
        }
    }

    open class BaseCommand(sender: ProxyCommandSender, successBox: CommandBox<Boolean>, completeBox: CommandBox<List<String>?>) :
        Command(sender, successBox, completeBox) {

        open fun complete(sender: ProxyCommandSender, context: CommandContext): List<String>? {
            run(sender, context, 0, false)
            return completeBox.value
        }

        open fun execute(sender: ProxyCommandSender, context: CommandContext): Boolean {
            run(sender, context, 0, true)
            return success
        }

        open fun execute(func: (CommandContext) -> Unit) {
            execute = { func(it) }
        }
    }

    open class ArgumentCommand(sender: ProxyCommandSender, successBox: CommandBox<Boolean>, completeBox: CommandBox<List<String>?>, val argument: String) :
        Command(sender, successBox, completeBox) {

        fun complete(func: (CommandContext) -> List<String>?) {
            complete = func
        }

        fun restrict(func: String.(CommandContext) -> Boolean) {
            restrict = func
        }

        open fun execute(func: String.(CommandContext) -> Unit) {
            execute = func
        }
    }
}