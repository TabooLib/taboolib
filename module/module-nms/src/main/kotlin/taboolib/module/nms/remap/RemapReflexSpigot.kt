package taboolib.module.nms.remap

import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor
import org.tabooproject.reflex.Reflection
import org.tabooproject.reflex.ReflexRemapper
import taboolib.module.nms.LightReflection
import taboolib.module.nms.MinecraftVersion
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.remap.RefRemapper
 *
 * @author sky
 * @since 2021/6/18 5:43 下午
 */
@Suppress("DuplicatedCode")
class RemapReflexSpigot : ReflexRemapper {

    var isUniversal = MinecraftVersion.isUniversal
    val major = MinecraftVersion.major
    val fieldRemapCacheMap = ConcurrentHashMap<String, String>()
    val methodRemapCacheMap = ConcurrentHashMap<String, String>()
    val descriptorTypeCacheMap = ConcurrentHashMap<String, List<Class<*>>>()

    val spigotMapping = MinecraftVersion.spigotMapping

    override fun field(name: String, field: String): String {
        // 1.17 开始字段混淆
        if (isUniversal) {
            val namespace = "$name#$field"
            return if (fieldRemapCacheMap.containsKey(namespace)) {
                fieldRemapCacheMap[namespace]!!
            } else {
                // 还原
                val value = spigotMapping.fields.find { it.path == name && it.translateName == field }?.mojangName ?: field
                fieldRemapCacheMap[namespace] = value
                value
            }
        }
        return field
    }

    override fun method(name: String, method: String, vararg parameter: Any?): String {
        // 1.18 开始方法混淆
        if (major >= 10) {
            val namespace = "$name#$method(${parameter.joinToString(",") { it?.javaClass?.name.toString() }})"
            return if (methodRemapCacheMap.containsKey(namespace)) {
                methodRemapCacheMap[namespace]!!
            } else {
                // 还原
                val value = spigotMapping.methods.find {
                    // 判断方法描述符获取准确方法
                    it.path == name && it.translateName == method && checkParameterType(it.descriptor, *parameter)
                }?.mojangName ?: method
                methodRemapCacheMap[namespace] = value
                value
            }
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
                            classes += LightReflection.forName(name.replace('/', '.'))
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