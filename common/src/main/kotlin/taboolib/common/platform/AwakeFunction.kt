package taboolib.common.platform

import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import java.lang.reflect.Method
import java.util.function.Supplier

class AwakeFunction(override val lifeCycle: LifeCycle) : Injector.Methods {

    override val priority: Byte
        get() = 0

    override fun inject(method: Method, clazz: Class<*>, instance: Supplier<*>) {
        if (method.isAnnotationPresent(Awake::class.java) && method.getAnnotation(Awake::class.java).value == lifeCycle) {
            method.invoke(instance.get())
        }
    }
}