package taboolib.module.nms

import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor
import org.tabooproject.reflex.Reflection
import org.tabooproject.reflex.ReflexRemapper
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.RefRemapper
 *
 * @author sky
 * @since 2021/6/18 5:43 下午
 */
object RefRemapper : ReflexRemapper {

    val methodRemapperCacheMap = ConcurrentHashMap<String, List<Class<*>>>()

    override fun field(name: String, field: String): String {
        if (MinecraftVersion.isUniversal) {
            return MinecraftVersion.mapping.fields.firstOrNull { it.path == name && it.translateName == field }?.mojangName ?: field
        }
        return field
    }

    override fun method(name: String, method: String, vararg parameter: Any?): String {
        // 1.18
        if (MinecraftVersion.major >= 10) {
            return MinecraftVersion.mapping.methods.firstOrNull {
                // 判断方法描述符获取准确方法
                it.path == name && it.translateName == method && checkParameterType(it.descriptor, *parameter)
            }?.mojangName ?: method
        }
        return method
    }

    fun checkParameterType(descriptor: String, vararg parameter: Any?): Boolean {
        return Reflection.isAssignableFrom(getParameterTypes(descriptor).toTypedArray(), parameter.map { p -> p?.javaClass }.toTypedArray())
    }

    fun getParameterTypes(descriptor: String): List<Class<*>> {
        return methodRemapperCacheMap.computeIfAbsent(descriptor) {
            val classes = ArrayList<Class<*>>()
            SignatureReader(descriptor).accept(object : SignatureVisitor(Opcodes.ASM9) {
                override fun visitParameterType(): SignatureVisitor {
                    return object : SignatureVisitor(Opcodes.ASM9) {
                        override fun visitClassType(name: String) {
                            classes += Class.forName(name.replace('/', '.'))
                            super.visitClassType(name)
                        }
                    }
                }
            })
            classes
        }
    }
}