package taboolib.module.nms.remap

import org.apache.commons.lang3.JavaVersion
import org.apache.commons.lang3.SystemUtils
import org.objectweb.asm.Type
import taboolib.common.reflect.ClassHelper
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.remap.RemapHelper
 *
 * @author 坏黑
 * @since 2024/7/21 23:23
 */
object RemapHelper {

    private val descriptorTypeCacheMap = ConcurrentHashMap<String, Array<Class<*>>>()
    private val autoboxing = SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_5)

    /**
     * 根据「方法描述符」中的「参数」检查与目标「方法描述符」中的「参数」是否匹配
     *
     * @param check 方法描述符（例如：(Ljava/lang/String;I)V）
     * @param to 目标方法描述符（例如：(Ljava/lang/String;I)V）
     */
    fun checkParameterType(check: String, to: String): Boolean {
        return ClassHelper.isAssignable(getParameterTypes(check), getParameterTypes(to), autoboxing)
    }

    /**
     * 根据「参数实例」检查与目标「方法描述符」是否匹配
     *
     * @param check 参数实例（例如："字符串", 1）
     * @param to 目标方法描述符（例如：(Ljava/lang/String;I)V）
     */
    fun checkParameterType(check: Array<Any?>, to: String): Boolean {
        // 不再使用 Reflex 的判断方式
        // 相比 Reflex 的 Reflection.isAssignableFrom，此方法是反向的：
        // 左侧（cls）为检查类：表示想要分配的类
        // 右侧（toClass）为目标类：表示想要分配的类
        // 因此，如果 cls 可以分配给 toClass，则返回 true
        return ClassHelper.isAssignable(check.map { p -> p?.javaClass }.toTypedArray(), getParameterTypes(to), autoboxing)
    }

    /**
     * 获取方法描述符中的参数类型
     */
    fun getParameterTypes(descriptor: String): Array<Class<*>> {
        return if (descriptorTypeCacheMap.containsKey(descriptor)) {
            descriptorTypeCacheMap[descriptor]!!
        } else {
            val classes = Type.getType(descriptor).argumentTypes.map { ClassHelper.getClass(it.className, false) }.toTypedArray()
            descriptorTypeCacheMap[descriptor] = classes
            classes
        }
    }
}