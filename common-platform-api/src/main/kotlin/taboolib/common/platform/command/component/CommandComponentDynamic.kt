package taboolib.common.platform.command.component

import taboolib.common.platform.command.CommandContext

class CommandComponentDynamic(val comment: String, index: Int, optional: Boolean, permission: String) : CommandComponent(index, optional, permission) {

    internal var commandRestrict: CommandRestrict<*>? = null
    internal var commandSuggestion: CommandSuggestion<*>? = null

    /**
     * 创建当前节点下的命令建议约束（自动取消建议）
     */
    fun <T> restrict(bind: Class<T>, function: (sender: T, context: CommandContext<T>, argument: String) -> Boolean): CommandComponentDynamic {
        this.commandRestrict = CommandRestrict(bind, function)
        this.commandSuggestion = null
        return this
    }

    /**
     * 创建当前节点下的命令建议（自动取消约束）
     */
    fun <T> suggestion(bind: Class<T>, uncheck: Boolean = false, function: (sender: T, context: CommandContext<T>) -> List<String>?): CommandComponentDynamic {
        this.commandSuggestion = CommandSuggestion(bind, uncheck, function)
        this.commandRestrict = null
        return this
    }

    /**
     * 创建当前节点下的命令建议约束
     */
    inline fun <reified T> restrict(noinline function: (sender: T, context: CommandContext<T>, argument: String) -> Boolean): CommandComponentDynamic {
        return restrict(T::class.java, function)
    }

    /**
     * 创建当前节点下的命令建议
     */
    inline fun <reified T> suggestion(uncheck: Boolean = false, noinline function: (sender: T, context: CommandContext<T>) -> List<String>?): CommandComponentDynamic {
        return suggestion(T::class.java, uncheck, function)
    }

    /**
     * 创建当前节点下的不约束命令建议
     */
    inline fun <reified T> suggestionUncheck(noinline function: (sender: T, context: CommandContext<T>) -> List<String>?): CommandComponentDynamic {
        return suggestion(T::class.java, true, function)
    }

    /**
     * 解除约束
     */
    fun removeRestrict(): CommandComponentDynamic {
        this.commandRestrict = null
        return this
    }

    /**
     * 解除建议
     */
    fun removeSuggestion(): CommandComponentDynamic {
        this.commandSuggestion = null
        return this
    }

    override fun toString(): String {
        return "CommandComponentDynamic(comment='$comment')"
    }
}