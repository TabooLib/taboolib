package taboolib.common.inject.impl

import org.tabooproject.reflex.ClassMethod
import taboolib.common.Internal
import taboolib.common.io.InstGetter
import taboolib.common.LifeCycle
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.submit

@Internal
@Awake
@Bind([Schedule::class], target = Bind.Target.METHOD)
object InjectorSchedule : Injector(LifeCycle.ACTIVE) {

    override fun inject(clazz: Class<*>, method: ClassMethod, instance: InstGetter<*>) {
        val obj = instance.get() ?: return
        val schedule = method.getAnnotation(Schedule::class.java)!!
        submit(async = schedule.property("async")!!, delay = schedule.property("delay")!!, period = schedule.property("period")!!) { method.invoke(obj) }
    }
}