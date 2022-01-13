package taboolib.module.kether

import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import taboolib.common.io.taboolibPath
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getOpenContainers
import taboolib.common.platform.function.pluginId
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
object KetherLoader : Injector.Methods {

    val sharedParser = ArrayList<Pair<Array<String>, String>>()
    val sharedScriptProperty = ArrayList<Pair<String, String>>()

    @Awake(LifeCycle.DISABLE)
    fun cancel() {
        getOpenContainers().forEach { remote ->
            sharedParser.forEach {
                remote.call(StandardChannel.REMOTE_REMOVE_ACTION, arrayOf(it.first, it.second))
            }
            sharedScriptProperty.forEach {
                remote.call(StandardChannel.REMOTE_REMOVE_PROPERTY, arrayOf(it.first, it.second))
            }
        }
    }

    override fun inject(method: Method, clazz: Class<*>, instance: Supplier<*>) {
        if (method.isAnnotationPresent(KetherParser::class.java) && method.returnType == ScriptActionParser::class.java) {
            val parser = method.invoke(instance.get()) as ScriptActionParser<*>
            val annotation = method.getAnnotation(KetherParser::class.java)
            if (annotation.shared) {
                sharedParser += annotation.value to annotation.namespace
                getOpenContainers().forEach {
                    it.call(StandardChannel.REMOTE_ADD_ACTION, arrayOf(pluginId, annotation.value, annotation.namespace))
                }
            }
            annotation.value.forEach {
                Kether.addAction(it, parser, annotation.namespace)
            }
        }
        if (method.isAnnotationPresent(KetherProperty::class.java) && method.returnType == ScriptProperty::class.java) {
            val property = method.invoke(instance.get()) as ScriptProperty<*>
            val annotation = method.getAnnotation(KetherProperty::class.java)
            if (annotation.shared) {
                var name = annotation.bind.java.name
                name = if (name.startsWith(taboolibPath)) "@${name.substring(taboolibPath.length)}" else name
                sharedScriptProperty += name to property.id
                getOpenContainers().forEach {
                    it.call(StandardChannel.REMOTE_ADD_PROPERTY, arrayOf(pluginId, name, property))
                }
            }
            Kether.addScriptProperty(annotation.bind.java, property)
        }
    }

    override val priority: Byte
        get() = 0

    override val lifeCycle: LifeCycle
        get() = LifeCycle.LOAD
}