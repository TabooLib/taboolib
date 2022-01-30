package taboolib.common.platform.command

abstract class Section(val optional: Boolean, val permission: String = "") {

    var executor: ActionExecute<*>? = null
        protected set

    val children = ArrayList<Section>()

    inline fun <reified T> execute(noinline function: (sender: T, context: CommandContext<T>, argument: String) -> Unit) {
        execute(T::class.java, function)
    }

    fun <T> execute(bind: Class<T>, function: (sender: T, context: CommandContext<T>, argument: String) -> Unit) {
        executor = ActionExecute(bind, function)
    }

    fun literal(vararg aliases: String, optional: Boolean = false, permission: String = "", literal: SectionLiteral.() -> Unit) {
        children += SectionLiteral(*aliases, optional = optional, permission = permission).also(literal)
    }

    fun dynamic(commit: String = "...", optional: Boolean = false, permission: String = "", dynamic: SectionDynamic.() -> Unit) {
        children += SectionDynamic(commit, optional, permission).also(dynamic)
    }

    fun children(context: CommandContext<*>): List<Section> {
        return children.filter { it.permission.isEmpty() || context.checkPermission(it.permission) }
    }
}