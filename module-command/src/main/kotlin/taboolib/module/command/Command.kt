package taboolib.module.command

/**
 * TabooLib
 * taboolib.module.command.Command
 *
 * @author sky
 * @since 2021/6/25 12:50 上午
 */
open class Command {

    fun literal(name: String, execute: Command.() -> Unit) {

    }

    fun required(name: String = "", execute: ArgumentCommand.() -> Unit) {

    }

    fun optional(name: String = "", execute: ArgumentCommand.() -> Unit) {

    }

    open class ArgumentCommand : Command() {

        val args: List<String>
            get() = emptyList()

        fun complete(func: () -> List<String>) {

        }

        fun restrict(func: String.() -> Boolean) {

        }
    }
}