package taboolib.common.platform

import org.tabooproject.reflex.ClassMethod
import taboolib.common.InstGetter
import taboolib.common.LifeCycle
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector
import taboolib.common.platform.function.submit
import java.lang.reflect.Method
import java.util.function.Supplier

@Awake
@Bind([Schedule::class], target = Bind.Target.METHOD)
object ScheduleLoader : Injector(LifeCycle.ACTIVE) {

    override fun inject(clazz: Class<*>, method: ClassMethod, instance: InstGetter<*>) {
        val obj = instance.get() ?: return
        val schedule = method.getAnnotation(Schedule::class.java)!!
        submit(async = schedule.property("async")!!, delay = schedule.property("delay")!!, period = schedule.property("period")!!) { method.invoke(obj) }
    }
}