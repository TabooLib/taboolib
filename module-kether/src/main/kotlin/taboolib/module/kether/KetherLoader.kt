package taboolib.module.kether

import io.izzel.kether.common.api.QuestActionParser
import taboolib.common.LifeCycle
import taboolib.common.OpenReceiver
import taboolib.common.inject.Injector
import taboolib.common.io.serialize
import taboolib.common.io.deserialize
import taboolib.common.platform.Awake
import taboolib.common.platform.getOpenContainers
import taboolib.common.util.asList
import java.io.Serializable
import java.lang.reflect.Method

/**
 * TabooLibKotlin
 * taboolib.module.kether.KetherLoader
 *
 * @author sky
 * @since 2021/2/6 3:33 下午
 */
@Awake
object KetherLoader : Injector.Methods, OpenReceiver {

    val registeredParser = ArrayList<ByteArray>()
    val registeredEventOperator = ArrayList<ByteArray>()
    val registeredPlayerOperator = ArrayList<ByteArray>()

    val openContainers by lazy {
        getOpenContainers()
    }

    @Awake(LifeCycle.DISABLE)
    fun cancel() {
        registeredParser.forEach { parser ->
            openContainers.forEach { it.unregister("openapi.kether.QuestActionParser", parser, emptyArray()) }
        }
        registeredEventOperator.forEach { parser ->
            openContainers.forEach { it.unregister("openapi.kether.EventOperator", parser, emptyArray()) }
        }
        registeredPlayerOperator.forEach { parser ->
            openContainers.forEach { it.unregister("openapi.kether.PlayerOperator", parser, emptyArray()) }
        }
    }

    override fun inject(method: Method, clazz: Class<*>, instance: Any?) {
        if (method.isAnnotationPresent(KetherParser::class.java)) {
            val parser = method.getAnnotation(KetherParser::class.java)
            val questActionParser = method.invoke(instance) as QuestActionParser
            if (questActionParser is Serializable && parser.shared) {
                val bytes = questActionParser.serialize {
                    writeObject(parser.value.toList())
                    writeUTF(parser.namespace)
                }
                openContainers.forEach { it.register("openapi.kether.QuestActionParser", bytes, emptyArray()) }
                registeredParser.add(bytes)
            } else {
                parser.value.forEach {
                    Kether.addAction(it, questActionParser, parser.namespace)
                }
            }
        }
    }

    override fun register(name: String, any: ByteArray, args: Array<out String>): Boolean {
        return when (name) {
            "openapi.kether.QuestActionParser" -> {
                var keys = emptyList<String>()
                var namespace = ""
                val parser = any.deserialize<QuestActionParser> {
                    keys = readObject().asList()
                    namespace = readUTF()
                }
                keys.forEach { Kether.addAction(it, parser, namespace) }
                true
            }
            "openapi.kether.EventOperator" -> {
                true
            }
            "openapi.kether.PlayerOperator" -> {
                true
            }
            else -> false
        }
    }

    override fun unregister(name: String, any: ByteArray, args: Array<out String>): Boolean {
        return when (name) {
            "openapi.kether.QuestActionParser" -> {
                var keys = emptyList<String>()
                var namespace = ""
                any.deserialize<QuestActionParser> {
                    keys = readObject().asList()
                    namespace = readUTF()
                }
                keys.forEach { Kether.removeAction(it, namespace) }
                true
            }
            "openapi.kether.EventOperator" -> {
                true
            }
            "openapi.kether.PlayerOperator" -> {
                true
            }
            else -> false
        }
    }

    override val priority: Byte
        get() = 0

    override val lifeCycle: LifeCycle
        get() = LifeCycle.LOAD
}