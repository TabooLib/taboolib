package taboolib.common.platform

import taboolib.common.inject.Injector
import java.lang.reflect.Method

@Awake
object EventBus : Injector.Methods {

    override fun inject(method: Method, clazz: Class<*>, instance: Any?) {
        if (method.isAnnotationPresent(SubscribeEvent::class.java) && method.parameterCount == 1) {
            val subscribeEvent = method.getAnnotation(SubscribeEvent::class.java)
            if (runningPlatform == Platform.SPONGE) {
                registerListener(method.parameterTypes[0], subscribeEvent.order, subscribeEvent.beforeModifications) {
                    method.invoke(instance, it)
                }
            } else {
                registerListener(method.parameterTypes[0], subscribeEvent.priority, subscribeEvent.ignoreCancelled) {
                    method.invoke(instance, it)
                }
            }
        }
    }
}