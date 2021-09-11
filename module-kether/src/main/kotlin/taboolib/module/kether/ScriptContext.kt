package taboolib.module.kether

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptCommandSender
import taboolib.library.kether.AbstractQuestContext

/**
 * Adyeshach
 * taboolib.module.ketherx.ScriptContext
 *
 * @author sky
 * @since 2021/1/20 10:39 上午
 */
open class ScriptContext(service: ScriptService, script: Script) : AbstractQuestContext<ScriptContext>(service, script, null) {

    lateinit var id: String

    var sender: ProxyCommandSender?
        set(value) {
            this["@Sender"] = value?.origin
        }
        get() {
            return adaptCommandSender(this["@Sender"] ?: return null)
        }

    var breakLoop: Boolean
        get() = get<Boolean>("@BreakLoop") == true
        set(value) {
            this["@BreakLoop"] = value
        }

    operator fun set(key: String, value: Any?) {
        rootFrame().variables().set(key, value)
    }

    operator fun <T> get(key: String, def: T? = null): T? {
        return rootFrame().variables().get<T>(key).orElse(def)
    }

    override fun createExecutor(): ScriptSchedulerExecutor {
        return ScriptSchedulerExecutor
    }

    companion object {

        fun create(script: Script, context: ScriptContext.() -> Unit = {}): ScriptContext {
            return ScriptContext(ScriptService, script).also(context)
        }
    }
}