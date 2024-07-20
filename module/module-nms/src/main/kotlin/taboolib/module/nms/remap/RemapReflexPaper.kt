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
 * 只有 Paper 1.20.6+ 才会启用该类
 *
 * @author sky
 * @since 2021/6/18 5:43 下午
 */
@Suppress("DuplicatedCode")
class RemapReflexPaper : ReflexRemapper {

    val major = MinecraftVersion.major
    val fieldRemapCacheMap = ConcurrentHashMap<String, String>()
    val methodRemapCacheMap = ConcurrentHashMap<String, String>()
    val descriptorTypeCacheMap = ConcurrentHashMap<String, List<Class<*>>>()

    val spigotMapping = MinecraftVersion.spigotMapping
    val paperMapping = MinecraftVersion.paperMapping

    override fun field(name: String, field: String): String {
        val namespace = "$name#$field"
        return if (fieldRemapCacheMap.containsKey(namespace)) {
            fieldRemapCacheMap[namespace]!!
        } else {
            // 还原
            val obf = spigotMapping.fields.find { it.path == name && it.translateName == field }?.mojangName
            // 重映射
            val deobf = paperMapping.fields.find { it.path == name && it.mojangName == obf }?.translateName ?: field
            fieldRemapCacheMap[namespace] = deobf
            deobf
        }
    }

    override fun method(name: String, method: String, vararg parameter: Any?): String {
        val namespace = "$name#$method(${parameter.joinToString(",") { it?.javaClass?.name.toString() }})"
        return if (methodRemapCacheMap.containsKey(namespace)) {
            methodRemapCacheMap[namespace]!!
        } else {
            // 还原
            val obf = spigotMapping.methods.find {
                // 判断方法描述符获取准确方法
                // 这里存在一个潜在问题，与 NMSProxy 不同的是无法确认它来自何种对照表
                it.path == name && it.translateName == method && checkParameterType(it.descriptor, *parameter)
            }?.mojangName ?: method
            // 重映射
            val deobf = paperMapping.methods.find {
                it.path == name && it.mojangName == obf && checkParameterType(it.descriptor, *parameter)
            }?.translateName ?: method
            methodRemapCacheMap[namespace] = deobf
            deobf
        }
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