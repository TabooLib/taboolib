package taboolib.internal

import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.Reflection
import taboolib.common.LifeCycle
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector
import taboolib.common.io.InstGetter
import taboolib.common.platform.Awake
import taboolib.common.platform.function.warning
import taboolib.common5.Coerce
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer

@Internal
@Awake
@Bind([ConfigNode::class], target = Bind.Target.FIELD)
object InjectorConfigNode : Injector(LifeCycle.INIT, 1) {

    override fun inject(clazz: Class<*>, field: ClassField, instance: InstGetter<*>) {
        val node = field.getAnnotation(ConfigNode::class.java)!!
        val bind = node.property<String>("bind")!!
        val file = InjectorConfig.configFileMap[bind]
        if (file == null) {
            warning("$bind not defined")
            return
        }
        file.nodes += field
        var data = file.configuration[node.property<String>("value")!!.ifEmpty { field.name }]
        if (field.type == ConfigNodeTransfer::class.java) {
            (field.get(instance.get()) as ConfigNodeTransfer<*, *>).update(data)
        } else {
            when (Reflection.getReferenceType(field.fieldType)) {
                Integer::class.java -> data = Coerce.toInteger(data)
                Character::class.java -> data = Coerce.toChar(data)
                java.lang.Byte::class.java -> data = Coerce.toByte(data)
                java.lang.Long::class.java -> data = Coerce.toLong(data)
                java.lang.Double::class.java -> data = Coerce.toDouble(data)
                java.lang.Float::class.java -> data = Coerce.toFloat(data)
                java.lang.Short::class.java -> data = Coerce.toShort(data)
                java.lang.Boolean::class.java -> data = Coerce.toBoolean(data)
            }
            field.set(instance.get(), data)
        }
    }
}