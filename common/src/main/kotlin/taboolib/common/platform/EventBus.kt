package taboolib.common.platform

import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import taboolib.common.Isolated
import java.lang.reflect.Method

@Awake
@Isolated
object EventBus : Injector.Methods {

    override fun inject(method: Method, clazz: Class<*>, instance: Any?) {
        if (method.isAnnotationPresent(SubscribeEvent::class.java) && method.parameterCount == 1) {
            val event = method.getAnnotation(SubscribeEvent::class.java)
            when (runningPlatform) {
                Platform.BUNGEE -> {
                    registerListener(method.parameterTypes[0], if (event.level != 0) event.level else event.priority.level, event.beforeModifications) {
                        method.invoke(instance, it)
                    }
                }
                Platform.SPONGE -> {
                    registerListener(method.parameterTypes[0], event.order, event.beforeModifications) {
                        method.invoke(instance, it)
                    }
                }
                else -> {
                    registerListener(method.parameterTypes[0], event.priority, event.ignoreCancelled) {
                        method.invoke(instance, it)
                    }
                }
            }
        }
    }

    override val priority: Byte
        get() = 0

    override val lifeCycle: LifeCycle
        get() = LifeCycle.ENABLE
}