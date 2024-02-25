package taboolib.module.configuration

import org.tabooproject.reflex.ClassField
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.warning
import taboolib.common.reflect.getAnnotationIfPresent
import taboolib.common.util.*
import taboolib.common5.Coerce
import java.util.function.Supplier

@Awake
class ConfigNodeLoader : ClassVisitor(2) {

    override fun visit(field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
        if (field.isAnnotationPresent(ConfigNode::class.java)) {
            val node = field.getAnnotation(ConfigNode::class.java)
            var bind = node.property("bind", "")
            // 获取默认文件名称
            if (bind.isEmpty()) {
                bind = clazz.getAnnotationIfPresent(ConfigNode::class.java)?.bind ?: "config.yml"
            }
            // 自动补全后缀
            if (!bind.contains('.')) {
                bind = "$bind.yml"
            }
            val file = ConfigLoader.files[bind]
            if (file == null) {
                warning("$bind not defined: $field")
                return
            }
            file.nodes += field
            // 绑定的节点
            val bindNode = node.property("value", "").ifEmpty { field.name.substringBefore('$').toNode() }
            var data = file.configuration[bindNode]
            if (data == null) {
                warning("$bindNode not found in $bind")
                return
            }
            // 类型转换工具
            if (field.fieldType == ConfigNodeTransfer::class.java) {
                val transfer = field.get(instance?.get()) as ConfigNodeTransfer<*, *>
                transfer.reset(data)
            } else {
                // 基本类型转换
                data = when (field.fieldType) {
                    Integer::class.java -> Coerce.toInteger(data)
                    Character::class.java -> Coerce.toChar(data)
                    JavaByte::class.java -> Coerce.toByte(data)
                    JavaLong::class.java -> Coerce.toLong(data)
                    JavaDouble::class.java -> Coerce.toDouble(data)
                    JavaFloat::class.java -> Coerce.toFloat(data)
                    JavaShort::class.java -> Coerce.toShort(data)
                    JavaBoolean::class.java -> Coerce.toBoolean(data)
                    else -> data
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