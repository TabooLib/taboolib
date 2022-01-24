package taboolib.common.platform

import org.tabooproject.reflex.ClassMethod
import taboolib.common.LifeCycle
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector
import taboolib.common.io.LazyInstGetter
import java.lang.reflect.Method
import java.util.function.Supplier

@Bind([], type = Bind.Type.METHOD, annotation = Awake::class)
class AwakeFunction(lifeCycle: LifeCycle) : Injector(lifeCycle) {

    override fun inject(clazz: Class<*>, method: ClassMethod, instance: LazyInstGetter<*>) {

    }

    override fun inject(method: Method, clazz: Class<*>, instance: Supplier<*>) {
        if (method.isAnnotationPresent(Awake::class.java) && method.getAnnotation(Awake::class.java).value == lifeCycle) {
            method.invoke(instance.get())
        }
    }
}