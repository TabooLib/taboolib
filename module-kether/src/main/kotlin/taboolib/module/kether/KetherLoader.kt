package taboolib.module.kether

import taboolib.common.LifeCycle
import taboolib.common.OpenListener
import taboolib.common.OpenResult
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import taboolib.common.platform.getOpenContainers
import taboolib.common.platform.pluginId
import java.lang.reflect.Method
import java.util.function.Supplier

/**
 * TabooLibKotlin
 * taboolib.module.kether.KetherLoader
 *
 * @author sky
 * @since 2021/2/6 3:33 下午
 */
@Suppress("UNCHECKED_CAST")
@Awake
object KetherLoader : Injector.Methods, OpenListener {

    val registeredParser = ArrayList<Pair<Array<String>, String>>()
    val registeredScriptProperty = ArrayList<String>()
    val registeredEvent = ArrayList<String>()

    @Awake(LifeCycle.DISABLE)
    fun cancel() {
//        registeredParser.forEach { parser ->
//            openContainers.forEach { it.unregister("openapi.kether.ScriptActionParser", arrayOf(parser.first, parser.second)) }
//        }
//        registeredScriptProperty.forEach { property ->
//            openContainers.forEach { it.unregister("openapi.kether.ScriptProperty", arrayOf(property)) }
//        }
//        registeredEvent.forEach { event ->
//            openContainers.forEach { it.unregister("openapi.kether.Event", arrayOf(event)) }
//        }
    }

    override fun inject(method: Method, clazz: Class<*>, instance: Supplier<*>) {
        if (method.isAnnotationPresent(KetherParser::class.java) && method.returnType == ScriptActionParser::class.java) {
            val parser = method.invoke(instance.get()) as ScriptActionParser<*>
            val annotation = method.getAnnotation(KetherParser::class.java)
            if (annotation.shared) {
                getOpenContainers().forEach {
                    it.call(StandardChannel.REMOTE_SHARED_ACTION, arrayOf(pluginId, annotation.value, annotation.namespace))
                }
            }
            annotation.value.forEach {
                Kether.addAction(it, parser, annotation.namespace)
            }
        }
    }

//    override fun inject(clazz: Class<*>, instance: Supplier<*>) {
//        if (clazz.isAnnotationPresent(KetherParser::class.java)) {
//            val parser = instance.get() as ScriptParser
//            val annotation = clazz.getAnnotation(KetherParser::class.java)
//            if (annotation.shared) {
//                info("share action ${annotation.value.toList()} (${annotation.namespace}) openContainers ${openContainers.map { it.name }}")
//                openContainers.forEach { it.call("openapi.kether.ScriptActionParser", arrayOf(parser.serialize(), annotation.value, annotation.namespace)) }
//                registeredParser.add(annotation.value to annotation.namespace)
//            } else {
//                annotation.value.forEach {
//                    Kether.addAction(it, parser, annotation.namespace)
//                }
//            }
//        }
//        if (clazz.isAnnotationPresent(KetherProperty::class.java)) {
//            val property = instance.get() as ScriptProperty
//            val annotation = clazz.getAnnotation(KetherProperty::class.java)
//            if (annotation.shared) {
//                openContainers.forEach { it.call("openapi.kether.ScriptProperty", arrayOf(property.serialize(), annotation.bind.java.name)) }
//                registeredScriptProperty.add(annotation.bind.java.name)
//            } else {
//                Kether.registeredScriptProperty.computeIfAbsent(annotation.bind.java) { ArrayList() } += property
//            }
//        }
//    }

    override fun call(name: String, data: Array<Any>): OpenResult {
//        info("register action $name ${data.map { it.javaClass.name }}")
//        when (name) {
//            "openapi.kether.ScriptActionParser" -> {
//                val parser = (data[0] as ByteArray).deserialize<ScriptParser>()
//                val keys = data[1] as Array<String>
//                val namespace = data[2].toString()
////                keys.forEach { Kether.addAction(it, ScriptActionParser<Any>(parser), namespace) }
//                true
//            }
//            "openapi.kether.ScriptProperty" -> {
//                val property = (data[0] as ByteArray).deserialize<ScriptProperty>()
//                val bind = data[1].toString()
//                try {
//                    Kether.addScriptProperty(Class.forName(bind), property)
//                } catch (ex: Exception) {
//                }
//                true
//            }
//            "openapi.kether.Event" -> {
//                try {
//                    Kether.registeredEvent[data[0].toString()] = Class.forName(data[1].toString())
//                    registeredEvent += data[1].toString()
//                } catch (ex: Exception) {
//                }
//                true
//            }
//            else -> false
//        }
        return OpenResult.failed()
    }

//    override fun unregister(name: String, data: Array<Any>): Boolean {
//        return when (name) {
//            "openapi.kether.ScriptActionParser" -> {
//                val keys = data[0] as Array<String>
//                val namespace = data[1].toString()
//                keys.forEach { Kether.removeAction(it, namespace) }
//                true
//            }
//            "openapi.kether.ScriptProperty" -> {
//                val property = data[0] as ScriptProperty
//                val bind = data[1].toString()
//                try {
//                    Kether.removeScriptProperty(Class.forName(bind), property)
//                } catch (ex: Exception) {
//                }
//                true
//            }
//            "openapi.kether.Event" -> {
//                try {
//                    Kether.registeredEvent.values.removeIf { it.name == data[0].toString() }
//                } catch (ex: Exception) {
//                }
//                true
//            }
//            else -> false
//        }
//    }

    override val priority: Byte
        get() = 0

    override val lifeCycle: LifeCycle
        get() = LifeCycle.LOAD
}