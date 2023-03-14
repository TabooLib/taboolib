package taboolib.module.kether

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptCommandSender

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
    var sandbox: Boolean = false,
    var detailError: Boolean = false,
    var context: ScriptContext.() -> Unit = {},
) {

    val vars = KetherShell.VariableMap(hashMapOf())

    class ScriptOptionsBuilder {

        private val options = ScriptOptions()

        /** 是否使用缓存 */
        fun useCache(useCache: Boolean) = apply { options.useCache = useCache }

        /** 命名空间 */
        fun namespace(namespace: List<String>) = apply { options.namespace = namespace }

        /** 缓存容器 */
        fun cache(cache: KetherShell.Cache) = apply { options.cache = cache }

        /** 脚本执行者 */
        fun sender(sender: Any) = apply { options.sender = if (sender is ProxyCommandSender) sender else adaptCommandSender(sender) }

        /** 设置变量 */
        fun vars(vars: KetherShell.VariableMap) = apply { options.vars.map += vars.map }

        /** 设置变量 */
        fun vars(vars: Map<String, Any?>) = apply { options.vars.map += vars }

        /** 设置变量 */
        fun vars(vararg vars: Pair<String, Any?>) = apply { options.vars.map += vars.toMap() }

        /** 设置变量 */
        fun set(key: String, value: Any?) = apply { options.vars.map[key] = value }

        /** 是否在沙盒中执行（不产生异常）*/
        fun sandbox(value: Boolean) = apply { options.sandbox = value }

        /** 是否打印详细的错误信息 */
        fun detailError(value: Boolean) = apply { options.detailError = value }

        /** 上下文回调函数 */
        fun context(context: ScriptContext.() -> Unit) = apply { options.context = context }

        fun build(): ScriptOptions {
            return options
        }
    }

    companion object {

        @JvmStatic
        fun builder() = ScriptOptionsBuilder()

        @JvmStatic
        fun new(const: ScriptOptionsBuilder.() -> Unit): ScriptOptions {
            return ScriptOptionsBuilder().also(const).build()
        }
    }
}