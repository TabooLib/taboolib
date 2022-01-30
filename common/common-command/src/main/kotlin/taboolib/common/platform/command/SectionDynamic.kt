package taboolib.common.platform.command

import taboolib.common.platform.function.onlinePlayers

open class SectionDynamic(val commit: String, optional: Boolean, permission: String) : Section(optional, permission) {

    var suggestion: ActionSuggestion<*>? = null
        protected set

    var restrict: ActionRestrict<*>? = null
        protected set

    inline fun <reified T> suggestPlayers() {
        suggestion<T> { _, _ -> onlinePlayers().map { it.name } }
    }

    inline fun <reified T> suggestion(uncheck: Boolean = false, noinline function: () -> List<String>?) {
        suggestion(T::class.java, uncheck, function)
    }

    inline fun <reified T> suggestion(uncheck: Boolean = false, noinline function: (sender: T, context: CommandContext<T>) -> List<String>?) {
        suggestion(T::class.java, uncheck, function)
    }

    inline fun <reified T> restrict(noinline function: (sender: T, context: CommandContext<T>, argument: String) -> Boolean) {
        restrict(T::class.java, function)
    }

    fun <T> suggestion(bind: Class<T>, uncheck: Boolean = false, function: () -> List<String>?) {
        this.suggestion = ActionSuggestion(bind, uncheck) { _, _ -> function() }
    }

    fun <T> suggestion(bind: Class<T>, uncheck: Boolean = false, function: (sender: T, context: CommandContext<T>) -> List<String>?) {
        this.suggestion = ActionSuggestion(bind, uncheck, function)
    }

    fun <T> restrict(bind: Class<T>, function: (sender: T, context: CommandContext<T>, argument: String) -> Boolean) {
        this.restrict = ActionRestrict(bind, function)
    }
}