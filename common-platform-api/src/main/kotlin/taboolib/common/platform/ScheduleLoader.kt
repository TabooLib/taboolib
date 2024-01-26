package taboolib.common.platform

import org.tabooproject.reflex.ClassMethod
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.function.submit
import taboolib.common.util.optional
import java.util.function.Supplier

@Awake
class ScheduleLoader : ClassVisitor(0) {

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.ACTIVE
    }

    override fun visit(method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        if (method.isAnnotationPresent(Schedule::class.java)) {
            val schedule = method.getAnnotation(Schedule::class.java)
            val obj = instance?.get()
            optional(schedule) {
                submit(async = schedule.property("async", false), delay = schedule.property("delay", 0), period = schedule.property("period", 0)) {
                    if (obj == null) {
                        method.invokeStatic()
                    } else {
                        method.invoke(obj)
                    }
                }
            }
        }
    }
}