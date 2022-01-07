package taboolib.module.kether

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.util.VariableReader

/**
 * your health {{player health}}, your name {{player name}}
 */
object KetherFunction {

    val reader = VariableReader()
    val mainCache = KetherShell.Cache()

    fun parse(
        input: List<String>,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        cache: KetherShell.Cache = mainCache,
        sender: ProxyCommandSender? = null,
        vars: KetherShell.VariableMap? = null,
        context: ScriptContext.() -> Unit = {},
    ): List<String> {
        return input.map { parse(it, cacheScript, namespace, cache, sender, vars, context) }
    }

    fun parse(
        input: String,
        cacheScript: Boolean = true,
        namespace: List<String> = emptyList(),
        cache: KetherShell.Cache = mainCache,
        sender: ProxyCommandSender? = null,
        vars: KetherShell.VariableMap? = null,
        context: ScriptContext.() -> Unit = {},
    ): String {
        return reader.replaceNested(input) {
            KetherShell.eval(this, cacheScript, namespace, cache, sender, vars, context).getNow(null).toString()
        }
    }
}