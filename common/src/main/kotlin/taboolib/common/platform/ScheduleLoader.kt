package taboolib.common.platform

import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import taboolib.common.platform.function.submit
import java.lang.reflect.Method
import java.util.function.Supplier

@Awake
object ScheduleLoader : Injector.Methods {

    override val priority: Byte
        get() = 0

    override val lifeCycle: LifeCycle
        get() = LifeCycle.ACTIVE

    override fun inject(method: Method, clazz: Class<*>, instance: Supplier<*>) {
        if (method.isAnnotationPresent(Schedule::class.java)) {
            val schedule = method.getAnnotation(Schedule::class.java)
            val obj = instance.get()
            submit(async = schedule.async, delay = schedule.delay, period = schedule.period) { method.invoke(obj) }
        }
    }
}