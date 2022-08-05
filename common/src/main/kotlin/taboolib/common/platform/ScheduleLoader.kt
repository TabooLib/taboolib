package taboolib.common.platform

import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ClassMethod
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.function.submit
import java.util.function.Supplier

@Awake
class ScheduleLoader : ClassVisitor(0) {

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.ACTIVE
    }

    override fun visit(method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        if (method.isAnnotationPresent(Schedule::class.java)) {
            val schedule = method.getAnnotation(Schedule::class.java)!!
            val obj = instance?.get()
            submit(async = schedule.property("async")!!, delay = schedule.property("delay")!!, period = schedule.property("period")!!) {
                if (obj == null) {
                    method.invokeStatic()
                } else {
                    method.invoke(obj)
                }
            }
        }
    }
}