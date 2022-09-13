package taboolib.module.configuration

import org.tabooproject.reflex.ClassField
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.warning
import taboolib.common.util.nonPrimitive
import taboolib.common5.Coerce
import java.util.function.Supplier

@Awake
class ConfigNodeLoader : ClassVisitor(2) {

    override fun visit(field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
        if (field.isAnnotationPresent(ConfigNode::class.java)) {
            val node = field.getAnnotation(ConfigNode::class.java)
            val bind = node.property("bind", "config.yml")
            val file = ConfigLoader.files[bind]
            if (file == null) {
                warning("$bind not defined: $field")
                return
            }
            file.nodes += field
            val value = node.property("value", "")
            var data = file.conf[value.ifEmpty { field.name.toNode() }]
            if (data == null) {
                warning("$value not found in $bind")
                return
            }
            if (field.fieldType == ConfigNodeTransfer::class.java) {
                val transfer = field.get(instance?.get()) as ConfigNodeTransfer<*, *>
                transfer.update(data)
            } else {
                when (field.fieldType.nonPrimitive()) {
                    Integer::class.java -> data = Coerce.toInteger(data)
                    Character::class.java -> data = Coerce.toChar(data)
                    java.lang.Byte::class.java -> data = Coerce.toByte(data)
                    java.lang.Long::class.java -> data = Coerce.toLong(data)
                    java.lang.Double::class.java -> data = Coerce.toDouble(data)
                    java.lang.Float::class.java -> data = Coerce.toFloat(data)
                    java.lang.Short::class.java -> data = Coerce.toShort(data)
                    java.lang.Boolean::class.java -> data = Coerce.toBoolean(data)
                }
                field.set(instance?.get(), data)
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.INIT
    }

    fun String.toNode(): String {
        return map { if (it.isUpperCase()) "-${it.lowercase()}" else it }.joinToString("")
    }
}