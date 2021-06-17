package taboolib.module.kether

import io.izzel.kether.common.actions.KetherTypes
import io.izzel.kether.common.api.QuestActionParser
import io.izzel.kether.common.loader.LoadError
import io.izzel.kether.common.loader.QuestReader
import taboolib.module.dependency.*
import taboolib.module.kether.action.game.PlayerOperator
import kotlin.reflect.KClass

@RuntimeDependencies(
    RuntimeDependency(group = "com.google.guava", id = "guava", version = "21.0", hash = "3a3d111be1be1b745edfa7d91678a12d7ed38709"),
    RuntimeDependency(group = "org.apache.commons", id = "commons-lang3", version = "3.5", hash = "6c6c702c89bfff3cd9e80b04d668c5e190d588c6")
)
@RuntimeNames(
    RuntimeName(group = "com.google.guava", name = "Guava (21.0)"),
    RuntimeName(group = "org.apache.commons", name = "Apache Commons (3.5)")
)
@RuntimeTests(
    RuntimeTest(group = "com.google.guava", path = ["com.google.common.base.Optional"]),
    RuntimeTest(group = "org.apache.commons", path = ["org.apache.commons.lang3.Validate"])
)
object Kether {

    val registry = ScriptService.INSTANCE.registry.also {
        KetherTypes.registerInternals(it, ScriptService.INSTANCE)
    }

    val operatorsEvent = LinkedHashMap<String, EventOperator<out Any>>()
    val operatorsPlayer = LinkedHashMap<String, PlayerOperator>()

    fun addAction(name: Array<String>, parser: QuestActionParser) {
        name.forEach { registry.registerAction(it, parser) }
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

    fun QuestReader.expects(vararg args: String): String {
        val element = nextToken()
        if (element !in args) {
            throw LoadError.NOT_MATCH.create("[${args.joinToString(", ")}]", element)
        }
        return element
    }
}