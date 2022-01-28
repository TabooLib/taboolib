package taboolib.module.kether

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.kether.QuestActionParser
import taboolib.library.kether.actions.KetherTypes
import taboolib.module.lang.Language

object Kether {

    @Awake(LifeCycle.INIT)
    fun init() {
        try {
            Language.textTransfer += KetherTransfer
        } catch (_: NoClassDefFoundError) {
        }
    }

    /**
     * 是否启用宽容解析器
     * 禁用时：
     * tell literal "HelloWorld!" 或 tell *"HelloWorld!"
     * 启用时：
     * tell "HelloWorld!"
     */
    var isAllowToleranceParser = true

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

    val registeredScriptProperty = HashMap<Class<*>, MutableMap<String, ScriptProperty<*>>>()
    val registeredPlayerOperator = LinkedHashMap<String, PlayerOperator>()

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

    internal fun addScriptProperty(clazz: Class<*>, property: ScriptProperty<*>) {
        registeredScriptProperty.computeIfAbsent(clazz) { HashMap() }[property.id] = property
    }

    internal fun removeScriptProperty(clazz: Class<*>, id: String) {
        registeredScriptProperty[clazz]?.remove(id)
    }
}