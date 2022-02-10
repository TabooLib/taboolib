package taboolib.internal

import org.tabooproject.reflex.ClassMethod
import taboolib.common.io.InstGetter
import taboolib.common.LifeCycle
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor

@Internal
@Awake
@Bind([Schedule::class], target = Bind.Target.METHOD)
object InjectorSchedule : Injector(LifeCycle.ACTIVE) {

    override fun inject(clazz: Class<*>, method: ClassMethod, instance: InstGetter<*>) {
        val obj = instance.get() ?: return
        val schedule = method.getAnnotation(Schedule::class.java)!!
        submit(async = schedule.property("async")!!, delay = schedule.property("delay")!!, period = schedule.property("period")!!) { method.invoke(obj) }
    }

    @Awake(LifeCycle.ENABLE)
    fun start() {
        PlatformFactory.getPlatformService<PlatformExecutor>().start()
    }
}