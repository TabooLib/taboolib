package taboolib.common.platform

import taboolib.common.io.classes

@Awake
@Suppress("NO_REFLECTION_IN_CLASS_PATH")
object EventBus {

    init {
        classes.forEach {
            try {
                inject(it, it.kotlin.objectInstance ?: return@forEach)
            } catch (ex: ExceptionInInitializerError) {
            }
        }
    }

    fun registerEvents(instance: Any) {
        inject(instance.javaClass, instance)
    }

    fun inject(clazz: Class<*>, instance: Any) {
        if (PlatformFactory.checkPlatform(clazz)) {
            clazz.declaredMethods.forEach { method ->
                if (method.isAnnotationPresent(SubscribeEvent::class.java) && method.parameterCount == 1) {
                    val subscribeEvent = method.getAnnotation(SubscribeEvent::class.java)
                    registerListener(method.parameterTypes[0], subscribeEvent.priority, subscribeEvent.ignoreCancelled) {
                        method.invoke(instance, it)
                    }
                }
            }
        }
    }
}