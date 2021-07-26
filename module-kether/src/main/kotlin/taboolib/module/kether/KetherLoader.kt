package taboolib.module.kether

import taboolib.common.LifeCycle
import taboolib.common.OpenReceiver
import taboolib.common.inject.Injector
import taboolib.common.io.deserialize
import taboolib.common.io.serialize
import taboolib.common.platform.Awake
import taboolib.common.platform.getOpenContainers
import taboolib.common.util.asList
import java.lang.Exception
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
    val registeredScriptProperty = ArrayList<ByteArray>()
    val registeredEvent = ArrayList<String>()

    val openContainers by lazy {
        getOpenContainers()
    }

    @Awake(LifeCycle.DISABLE)
    fun cancel() {
        registeredParser.forEach { parser ->
            openContainers.forEach { it.unregister("openapi.kether.ScriptActionParser", parser, emptyArray()) }
        }
        registeredScriptProperty.forEach { parser ->
            openContainers.forEach { it.unregister("openapi.kether.ScriptProperty", parser, emptyArray()) }
        }
        registeredEvent.forEach { event ->
            openContainers.forEach { it.unregister("openapi.kether.Event", ByteArray(0), arrayOf(event)) }
        }
    }

    override fun inject(method: Method, clazz: Class<*>, instance: Any) {
        if (method.isAnnotationPresent(KetherParser::class.java)) {
            val parser = method.invoke(instance) as ScriptActionParser<*>
            val annotation = method.getAnnotation(KetherParser::class.java)
            if (annotation.shared) {
                val bytes = parser.serialize {
                    writeObject(annotation.value.toList())
                    writeUTF(annotation.namespace)
                }
                openContainers.forEach { it.register("openapi.kether.ScriptActionParser", bytes, emptyArray()) }
                registeredParser.add(bytes)
            } else {
                annotation.value.forEach {
                    Kether.addAction(it, parser, annotation.namespace)
                }
            }
        }
        if (method.isAnnotationPresent(KetherProperty::class.java)) {
            val property = method.invoke(instance) as ScriptProperty
            val annotation = method.getAnnotation(KetherProperty::class.java)
            if (annotation.shared) {
                val bytes = property.serialize {
                    writeUTF(annotation.bind.java.name)
                }
                openContainers.forEach { it.register("openapi.kether.ScriptProperty", bytes, emptyArray()) }
                registeredScriptProperty.add(bytes)
            } else {
                Kether.registeredScriptProperty.computeIfAbsent(annotation.bind.java) { ArrayList() } += property
            }
        }
    }

    override fun register(name: String, any: ByteArray, args: Array<out String>): Boolean {
        return when (name) {
            "openapi.kether.ScriptActionParser" -> {
                var keys = emptyList<String>()
                var namespace = ""
                val parser = any.deserialize<ScriptActionParser<*>> {
                    keys = readObject().asList()
                    namespace = readUTF()
                }
                keys.forEach { Kether.addAction(it, parser, namespace) }
                true
            }
            "openapi.kether.ScriptProperty" -> {
                var bind = ""
                val property = any.deserialize<ScriptProperty> {
                    bind = readUTF()
                }
                try {
                    Kether.addScriptProperty(Class.forName(bind), property)
                } catch (ex: Exception) {
                }
                true
            }
            "openapi.kether.Event" -> {
                try {
                    Kether.registeredEvent[args[0]] = Class.forName(args[1])
                    registeredEvent += args[1]
                } catch (ex: Exception) {
                }
                true
            }
            else -> false
        }
    }

    override fun unregister(name: String, any: ByteArray, args: Array<out String>): Boolean {
        return when (name) {
            "openapi.kether.ScriptActionParser" -> {
                var keys = emptyList<String>()
                var namespace = ""
                any.deserialize<ScriptActionParser<*>> {
                    keys = readObject().asList()
                    namespace = readUTF()
                }
                keys.forEach { Kether.removeAction(it, namespace) }
                true
            }
            "openapi.kether.ScriptProperty" -> {
                var bind = ""
                val property = any.deserialize<ScriptProperty> {
                    bind = readUTF()
                }
                try {
                    Kether.removeScriptProperty(Class.forName(bind), property)
                } catch (ex: Exception) {
                }
                true
            }
            "openapi.kether.Event" -> {
                try {
                    Kether.registeredEvent.values.removeIf { it.name == args[0] }
                } catch (ex: Exception) {
                }
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