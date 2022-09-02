package taboolib.module.kether

import org.tabooproject.reflex.ClassMethod
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.io.taboolibPath
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getOpenContainers
import taboolib.common.platform.function.pluginId
import taboolib.common.util.asList
import java.util.function.Supplier

/**
 * TabooLibKotlin
 * taboolib.module.kether.KetherLoader
 *
 * @author sky
 * @since 2021/2/6 3:33 下午
 */
@Awake
class KetherLoader : ClassVisitor(0) {

    override fun visit(method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        if (method.isAnnotationPresent(KetherParser::class.java) && method.returnType == ScriptActionParser::class.java) {
            val parser = (if (instance == null) method.invokeStatic() else method.invoke(instance.get())) as ScriptActionParser<*>
            val annotation = method.getAnnotation(KetherParser::class.java)
            val value = annotation.property<Any>("value")?.asList()?.toTypedArray() ?: arrayOf()
            val namespace = annotation.property("namespace", "kether")
            if (annotation.property("shared", false)) {
                sharedParser += value to namespace
                getOpenContainers().forEach { it.call(StandardChannel.REMOTE_ADD_ACTION, arrayOf(pluginId, value, namespace)) }
            }
            value.forEach { Kether.addAction(it, parser, namespace) }
        } else if (method.isAnnotationPresent(KetherProperty::class.java) && method.returnType == ScriptProperty::class.java) {
            val property = (if (instance == null) method.invokeStatic() else method.invoke(instance.get())) as ScriptProperty<*>
            val annotation = method.getAnnotation(KetherProperty::class.java)
            val bind = annotation.property<Class<*>>("bind") ?: error("KetherProperty bind is null")
            if (annotation.property("shared", false)) {
                var name = bind.name
                name = if (name.startsWith(taboolibPath)) "@${name.substring(taboolibPath.length)}" else name
                sharedScriptProperty += name to property.id
                getOpenContainers().forEach { it.call(StandardChannel.REMOTE_ADD_PROPERTY, arrayOf(pluginId, name, property)) }
            }
            Kether.addScriptProperty(bind, property)
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.LOAD
    }

    companion object {

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
    }
}