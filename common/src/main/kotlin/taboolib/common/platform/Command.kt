package taboolib.common.platform

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

    fun literal(name: String, literal: Command.(CommandContext) -> Unit) {
        this.literals[name] = literal
    }

    fun required(required: ArgumentCommand.(CommandContext) -> Unit) {
        this.required = required
        this.optional = null
    }

    fun optional(optional: ArgumentCommand.(CommandContext) -> Unit) {
        this.optional = optional
        this.required = null
    }

    protected fun end(): Boolean {
        return literals.isEmpty() && required == null && optional == null
    }

    protected fun run(sender: ProxyCommandSender, context: CommandContext, index: Int, inExecute: Boolean) {
    }

    open class BaseCommand(sender: ProxyCommandSender, successBox: CommandBox<Boolean>, completeBox: CommandBox<List<String>?>) :
        Command(sender, successBox, completeBox) {

        open fun complete(sender: ProxyCommandSender, context: CommandContext): List<String>? {
            run(sender, context, 0, false)
            return if (completeBox.value?.isEmpty() == true) null else completeBox.value
        }

        open fun execute(sender: ProxyCommandSender, context: CommandContext): Boolean {
            run(sender, context, 0, true)
            return success
        }

        open fun execute(func: (CommandContext) -> Unit) {
            execute = { func(it) }
        }
    }

    open class ArgumentCommand(sender: ProxyCommandSender, b1: CommandBox<Boolean>, b2: CommandBox<List<String>?>, val parent: Command, val argument: String) :
        Command(sender, b1, b2) {

        fun complete(func: (CommandContext) -> List<String>?) {
            parent.complete = func
        }

        fun restrict(func: String.(CommandContext) -> Boolean) {
            restrict = func
        }

        open fun execute(func: String.(CommandContext) -> Unit) {
            execute = func
        }
    }
}