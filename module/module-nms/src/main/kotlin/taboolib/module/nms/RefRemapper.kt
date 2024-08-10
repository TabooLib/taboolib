package taboolib.module.nms

import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor
import org.tabooproject.reflex.Reflection
import org.tabooproject.reflex.ReflexRemapper
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.RefRemapper
 *
 * @author sky
 * @since 2021/6/18 5:43 下午
 */
object RefRemapper : ReflexRemapper {

    var isUniversal = MinecraftVersion.isUniversal
    val major = MinecraftVersion.major
    val mapping = MinecraftVersion.mapping
    val fieldRemapCacheMap = ConcurrentHashMap<String, Optional<String>>()
    val methodRemapCacheMap = ConcurrentHashMap<String, Optional<String>>()
    val descriptorTypeCacheMap = ConcurrentHashMap<String, List<Class<*>>>()

    override fun field(name: String, field: String): String {
        // 1.17 开始字段混淆
        if (isUniversal) {
            val namespace = "$name#$field"
            return fieldRemapCacheMap.getOrPut(namespace) {
                Optional.ofNullable(
                    mapping.fields.firstOrNull { it.path == name && it.translateName == field }?.mojangName
                )
            }.orElse(field)
        }
        return field
    }

    override fun method(name: String, method: String, vararg parameter: Any?): String {
        // 1.18 开始方法混淆
        if (major >= 10) {
            val namespace = "$name#$method(${parameter.joinToString(",") { it?.javaClass?.name.toString() }})"

            return methodRemapCacheMap.getOrPut(namespace) {
                Optional.ofNullable(
                    mapping.methods.firstOrNull {
                        // 判断方法描述符获取准确方法
                        it.path == name && it.translateName == method && checkParameterType(it.descriptor, *parameter)
                    }?.mojangName
                )
            }.orElse(method)
        }
        return method
    }

    fun checkParameterType(descriptor: String, vararg parameter: Any?): Boolean {
        return Reflection.isAssignableFrom(getParameterTypes(descriptor).toTypedArray(), parameter.map { p -> p?.javaClass }.toTypedArray())
    }

    fun getParameterTypes(descriptor: String): List<Class<*>> {
        return if (descriptorTypeCacheMap.containsKey(descriptor)) {
            descriptorTypeCacheMap[descriptor]!!
        } else {
            val classes = LinkedList<Class<*>>()
            SignatureReader(descriptor).accept(object : SignatureVisitor(Opcodes.ASM7) {
                override fun visitParameterType(): SignatureVisitor {
                    return object : SignatureVisitor(Opcodes.ASM7) {
                        override fun visitClassType(name: String) {
                            classes += Class.forName(name.replace('/', '.'))
                            super.visitClassType(name)
                        }
                    }
                }
            })
            descriptorTypeCacheMap[descriptor] = classes
            classes
        }
    }
}