package taboolib.module.kether

import taboolib.common.platform.ProxyCommandSender

/**
 * TabooLib
 * taboolib.module.kether.ScriptOptions
 *
 * @author 坏黑
 * @since 2022/9/4 10:06
 */
class ScriptOptions(
    var useCache: Boolean = true,
    var namespace: List<String> = emptyList(),
    var cache: KetherShell.Cache = KetherShell.mainCache,
    var sender: ProxyCommandSender? = null,
    var vars: KetherShell.VariableMap? = null,
    var exception: Boolean = true,
    var context: ScriptContext.() -> Unit = {},
) {

    class ScriptOptionsBuilder {

        private val options = ScriptOptions()

        fun useCache(useCache: Boolean) = apply { options.useCache = useCache }

        fun namespace(namespace: List<String>) = apply { options.namespace = namespace }

        fun cache(cache: KetherShell.Cache) = apply { options.cache = cache }

        fun sender(sender: ProxyCommandSender?) = apply { options.sender = sender }

        fun vars(vars: KetherShell.VariableMap?) = apply { options.vars = vars }

        fun vars(vars: Map<String, Any?>) = apply { options.vars = KetherShell.VariableMap(vars) }

        fun exception(exception: Boolean) = apply { options.exception = exception }

        fun context(context: ScriptContext.() -> Unit) = apply { options.context = context }

        fun build(): ScriptOptions {
            return options
        }
    }

    companion object {

        @JvmStatic
        fun builder() = ScriptOptionsBuilder()
    }
}