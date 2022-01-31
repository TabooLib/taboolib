package taboolib.internal

import org.tabooproject.reflex.ClassMethod
import taboolib.common.LifeCycle
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector
import taboolib.common.io.InstGetter
import taboolib.common.platform.Awake
import taboolib.common.platform.event.ProxyListenerRegister
import taboolib.common.platform.event.SubscribeEvent

@Awake
@Bind([SubscribeEvent::class], target = Bind.Target.METHOD)
object InjectorSubscribeEvent : Injector(LifeCycle.ENABLE) {

    override fun inject(clazz: Class<*>, method: ClassMethod, instance: InstGetter<*>) {
        if (method.parameter.size == 1) {
            ProxyListenerRegister.INSTANCE.register(method.getAnnotation(SubscribeEvent::class.java)!!, method, instance)
        }
    }
}