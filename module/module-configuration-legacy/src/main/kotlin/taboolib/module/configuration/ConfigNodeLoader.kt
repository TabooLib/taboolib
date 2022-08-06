package taboolib.module.configuration

import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ClassMethod
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.warning
import java.util.function.Supplier

@Awake
class ConfigNodeLoader : ClassVisitor(1) {

    override fun visit(field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
        if (field.isAnnotationPresent(ConfigNode::class.java)) {
            val node = field.getAnnotation(ConfigNode::class.java)
            val bind = node.property("bind", "config.yml")
            val file = ConfigLoader.files[bind]
            if (file == null) {
                warning("$bind not defined")
                return
            }
            file.nodes += field
            val value = node.property("value", "")
            val data = file.conf[value.ifEmpty { field.name.toNode() }]
            if (data == null) {
                warning("$value not found in $bind")
                return
            }
            if (field.fieldType == ConfigNodeTransfer::class.java) {
                val transfer = field.get(instance?.get()) as ConfigNodeTransfer<*, *>
                transfer.update(data)
            } else {
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