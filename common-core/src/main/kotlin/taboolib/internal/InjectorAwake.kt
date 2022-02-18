package taboolib.internal

import org.tabooproject.reflex.ClassMethod
import taboolib.common.LifeCycle
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector
import taboolib.common.io.InstGetter
import taboolib.common.platform.Awake

@Internal
@Bind([Awake::class], target = Bind.Target.METHOD)
class InjectorAwake(lifeCycle: LifeCycle) : Injector(lifeCycle) {

    private val exception = IllegalArgumentException("value of Awake annotation must be present to inject")

    @Throws(IllegalArgumentException::class)
    override fun inject(clazz: Class<*>, method: ClassMethod, instance: InstGetter<*>) {
        (method.getAnnotation(Awake::class.java) ?: throw exception)
            .enum<LifeCycle>("value")
            .takeIf { it == LifeCycle.INIT }
            ?.let { _ -> instance.get()?.let { method.invoke(it) } }
    }
}
