package taboolib.common.platform.command.component

import taboolib.common.platform.command.CommandContext

abstract class CommandComponent(val index: Int, var optional: Boolean, val permission: String = "") {

    /** 命令执行器 */
    internal var commandExecutor: CommandExecutor<*>? = null

    /** 下层节点 */
    val children = ArrayList<CommandComponent>()

    /** 上层节点 */
    var parent: CommandComponent? = null

    /**
     * 添加一层明文节点
     */
    fun literal(
        vararg aliases: String,
        optional: Boolean = false,
        permission: String = "",
        hidden: Boolean = false,
        literal: CommandComponentLiteral.() -> Unit = {}
    ): CommandComponentLiteral {
        val component = CommandComponentLiteral(arrayOf(*aliases), hidden, index + 1, optional, permission).also(literal).also { it.parent = this }
        // 如果当前节点已存在命令执行器
        // 则自动视为可选节点
        if (commandExecutor != null) {
            component.optional = true
        }
        children += component
        return component
    }

    /**
     * 添加一层动态节点
     */
    fun dynamic(
        comment: String = "...",
        optional: Boolean = false,
        permission: String = "",
        dynamic: CommandComponentDynamic.() -> Unit = {}
    ): CommandComponentDynamic {
        val component = CommandComponentDynamic(comment, index + 1, optional, permission).also(dynamic).also { it.parent = this }
        // 如果当前节点已存在命令执行器
        // 则自动视为可选节点
        if (commandExecutor != null) {
            component.optional = true
        }
        children += component
        return component
    }

    /**
     * 创建当前节点下的简化执行器
     *
     * @param function 执行函数
     */
    inline fun <reified T> exec(noinline function: ExecuteContext<T>.() -> Unit) {
        execute(T::class.java) { sender, context, argument -> ExecuteContext(sender, context, argument).function() }
    }

    /**
     * 创建当前节点下的执行器
     *
     * @param bind 执行者类
     * @param function 执行函数
     */
    fun <T> execute(bind: Class<T>, function: (sender: T, context: CommandContext<T>, argument: String) -> Unit) {
        this.commandExecutor = CommandExecutor(bind, function)
        this.children.forEach { it.optional = true }
    }

    /**
     * 创建当前节点下的执行器
     *
     * @param function 执行函数
     */
    inline fun <reified T> execute(noinline function: (sender: T, context: CommandContext<T>, argument: String) -> Unit) {
        execute(T::class.java, function)
    }

    /**
     * 根据命令上下文获取所有合法的下层节点
     */
    fun findChildren(context: CommandContext<*>): List<CommandComponent> {
        return children.filter { it.permission.isEmpty() || context.checkPermission(it.permission) }
    }

    /**
     * 根据命令上下文和输入参数获取合法的下层节点
     */
    fun findChildren(context: CommandContext<*>, parameter: String): CommandComponent? {
        return findChildren(context).firstOrNull {
            context.currentComponent = it
            when (it) {
                // 明文节点
                is CommandComponentLiteral -> it.aliases.any { a -> a.equals(parameter, true) }
                // 动态节点
                is CommandComponentDynamic -> {
                    val suggestion = it.commandSuggestion
                    when {
                        // 若当前输入参数为空
                        parameter.isEmpty() -> false
                        // 若不满足约束
                        it.commandRestrict?.exec(context, parameter) == false -> false
                        // 若不满足建议（启用约束建议）
                        suggestion?.uncheck == false && suggestion.exec(context)?.none { s -> s == parameter } == true -> false
                        // 通过
                        else -> true
                    }
                }
                else -> error("Unknown component: ${it.javaClass.name}")
            }
        }
    }
}