package taboolib.module.kether

import io.izzel.kether.common.actions.KetherTypes
import io.izzel.kether.common.api.QuestActionParser
import io.izzel.kether.common.loader.LoadError
import io.izzel.kether.common.loader.QuestReader
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.module.lang.Language
import kotlin.reflect.KClass

@RuntimeDependencies(
    RuntimeDependency("!com.google.guava:guava:21.0", test = "!com.google.common.base.Optional"),
)
object Kether {

    init {
        try {
            Language.textTransfer += KetherTransfer
        } catch (ex: Throwable) {
        }
    }

    val registry by lazy {
        try {
            ScriptService.registry.also {
                KetherTypes.registerInternals(it, ScriptService)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            error(ex.toString())
        }
    }

    val operatorsEvent = LinkedHashMap<String, EventOperator<out Any>>()
    val operatorsPlayer = LinkedHashMap<String, PlayerOperator>()

    fun addAction(name: Array<String>, parser: QuestActionParser) {
        name.forEach { addAction(it, parser) }
    }

    fun addAction(name: String, parser: QuestActionParser, namespace: String? = null) {
        registry.registerAction(namespace ?: "kether", name, parser)
    }

    fun removeAction(name: String, namespace: String? = null) {
        registry.unregisterAction(namespace ?: "kether", name)
    }

    fun addPlayerOperator(name: String, operator: PlayerOperator) {
        operatorsPlayer[name] = operator
    }

    fun <T : Any> addEventOperator(name: String, event: KClass<out T>, func: EventOperator<T>.() -> Unit = {}) {
        operatorsEvent[name] = EventOperator(event).also(func)
    }

    fun getEventOperator(name: String): EventOperator<out Any>? {
        return operatorsEvent.entries.firstOrNull { it.key.equals(name, true) }?.value
    }
}