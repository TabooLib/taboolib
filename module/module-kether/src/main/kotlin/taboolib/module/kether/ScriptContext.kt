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

    /** 脚本执行者 */
    var sender: ProxyCommandSender?
        get() = get<Any?>("@Sender")?.let { adaptCommandSender(it) }
        set(value) {
            this["@Sender"] = value?.origin
        }

    /** 是否跳出循环 */
    var breakLoop: Boolean
        get() = get<Boolean>("@BreakLoop") == true
        set(value) {
            this["@BreakLoop"] = value
        }

    /** 设置变量 */
    operator fun set(key: String, value: Any?) {
        rootFrame().variables().set(key, value)
    }

    /** 获取变量 */
    operator fun <T> get(key: String, def: T? = null): T? {
        return rootFrame().variables().get<T>(key).orElse(def)
    }

    /** 创建脚本执行器 */
    override fun createExecutor(): ScriptSchedulerExecutor {
        return ScriptSchedulerExecutor
    }

    companion object {

        fun create(script: Script, context: ScriptContext.() -> Unit = {}): ScriptContext {
            return ScriptContext(ScriptService, script).also(context)
        }
    }
}