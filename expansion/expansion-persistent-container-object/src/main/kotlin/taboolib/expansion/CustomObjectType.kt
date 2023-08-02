package taboolib.expansion

import taboolib.common.LifeCycle
import taboolib.common.io.runningClasses
import taboolib.common.platform.Awake
import java.util.concurrent.ConcurrentHashMap

object CustomObjectType {

    /** 保证线程安全 Key无作用 */
    val types = ConcurrentHashMap<Class<*>, CustomTypeData>()

    fun getData(target: Any): CustomTypeData? {
        return types.values.firstOrNull {
            it.isThis(target)
        }
    }

    fun getDataByClass(target: Class<*>): CustomTypeData? {
        return types.values.firstOrNull {
            it.isThisByClass(target)
        }
    }

    @Awake(LifeCycle.LOAD)
    fun eval() {
        runningClasses.forEach { clazz ->
            if (clazz.isAnnotationPresent(CustomType::class.java)) {
                if (clazz.isAssignableFrom(CustomTypeData::class.java)) {
                    error("Class '${clazz.name}' must implement CustomTypeData.")
                }
                val function = clazz.getMethod("register")
                function.isAccessible = true

                val target = clazz.getDeclaredConstructor()
                target.isAccessible = true
                val instance = target.newInstance()
                function.invoke(instance)
            }
        }
    }

}
