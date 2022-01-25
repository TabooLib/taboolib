package taboolib.common.platform

import org.tabooproject.reflex.ClassMethod
import taboolib.common.InstGetter
import taboolib.common.LifeCycle
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector

@Bind([Awake::class], target = Bind.Target.METHOD)
class AwakeFunction(lifeCycle: LifeCycle) : Injector(lifeCycle) {

    override fun inject(clazz: Class<*>, method: ClassMethod, instance: InstGetter<*>) {
        if (method.getAnnotation(Awake::class.java)!!.enum<LifeCycle>("value") == lifeCycle) {
            method.invoke(instance.get() ?: return)
        }
    }
}