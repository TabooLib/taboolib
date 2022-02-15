package taboolib.internal

import org.tabooproject.reflex.ClassMethod
import taboolib.common.io.InstGetter
import taboolib.common.LifeCycle
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake

@Internal
@Bind([Awake::class], target = Bind.Target.METHOD)
class InjectorAwake(lifeCycle: LifeCycle) : Injector(lifeCycle) {

    override fun inject(clazz: Class<*>, method: ClassMethod, instance: InstGetter<*>) {
        if (method.getAnnotation(Awake::class.java)!!.enum<LifeCycle>("value") == lifeCycle) {
            method.invoke(instance.get() ?: return)
        }
    }
}
