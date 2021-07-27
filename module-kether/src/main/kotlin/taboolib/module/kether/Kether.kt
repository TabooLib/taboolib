package taboolib.module.kether

import io.izzel.kether.common.actions.KetherTypes
import io.izzel.kether.common.api.QuestActionParser
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.module.lang.Language

@RuntimeDependencies(
    RuntimeDependency("!com.google.guava:guava:21.0", test = "!com.google.common.base.Optional")
)
object Kether {

    init {
        try {
            Language.textTransfer += KetherTransfer
        } catch (ex: NoClassDefFoundError) {
        }
    }

    val scriptService by lazy {
        ScriptService
    }

    val scriptRegistry by lazy {
        try {
            ScriptService.registry.also {
                KetherTypes.registerInternals(it, scriptService)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            error(ex.toString())
        }
    }

    val registeredScriptProperty = HashMap<Class<*>, MutableList<ScriptProperty>>()
    val registeredPlayerOperator = LinkedHashMap<String, PlayerOperator>()
    val registeredEvent = HashMap<String, Class<*>>()

    internal fun addAction(name: Array<String>, parser: QuestActionParser) {
        name.forEach { addAction(it, parser) }
    }

    internal fun addAction(name: String, parser: QuestActionParser, namespace: String? = null) {
        scriptRegistry.registerAction(namespace ?: "kether", name, parser)
    }

    internal fun removeAction(name: String, namespace: String? = null) {
        scriptRegistry.unregisterAction(namespace ?: "kether", name)
    }

    internal fun addPlayerOperator(name: String, operator: PlayerOperator) {
        registeredPlayerOperator[name] = operator
    }

    internal fun addScriptProperty(clazz: Class<*>, property: ScriptProperty) {
        registeredScriptProperty.computeIfAbsent(clazz) { ArrayList() } += property
    }

    internal fun removeScriptProperty(clazz: Class<*>, property: ScriptProperty) {
        registeredScriptProperty[clazz]?.remove(property)
    }

    inline fun <reified T> addEvent(name: String) {
        KetherLoader.openContainers.forEach {
            it.register("openapi.kether.Event", ByteArray(0), arrayOf(name, T::class.java.name))
        }
    }

    inline fun <reified T> removeEvent() {
        KetherLoader.openContainers.forEach {
            it.unregister("openapi.kether.Event", ByteArray(0), arrayOf(T::class.java.name))
        }
    }

    fun getEvent(name: String): Class<*>? {
        return registeredEvent[name]
    }
}