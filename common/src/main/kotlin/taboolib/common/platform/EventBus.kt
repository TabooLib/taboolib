package taboolib.common.platform

import taboolib.common.Isolated
import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import java.lang.reflect.Method

@Awake
@Isolated
object EventBus : Injector.Methods {

    override fun inject(method: Method, clazz: Class<*>, instance: Any?) {
        if (method.isAnnotationPresent(SubscribeEvent::class.java) && method.parameterCount == 1) {
            val event = method.getAnnotation(SubscribeEvent::class.java)
            val eventBind = if (event.bind.isNotEmpty()) {
                try {
                    Class.forName(event.bind)
                } catch (ex: Throwable) {
                    null
                }
            } else {
                null
            }
            when (runningPlatform) {
                Platform.BUNGEE -> {
                    val level = if (event.level != 0) event.level else event.priority.level
                    if (eventBind != null) {
                        if (method.parameterTypes[0] == OptionalEvent::class.java) {
                            registerListener(eventBind, level, event.beforeModifications) { method.invoke(instance, OptionalEvent(it)) }
                        } else {
                            error("unsupported parameter for optional event")
                        }
                    } else {
                        registerListener(method.parameterTypes[0], level, event.beforeModifications) { method.invoke(instance, it) }
                    }
                }
                Platform.SPONGE -> {
                    if (eventBind != null) {
                        if (method.parameterTypes[0] == OptionalEvent::class.java) {
                            registerListener(eventBind, event.order, event.beforeModifications) { method.invoke(instance, OptionalEvent(it)) }
                        } else {
                            error("unsupported parameter for optional event")
                        }
                    } else {
                        registerListener(method.parameterTypes[0], event.order, event.beforeModifications) { method.invoke(instance, it) }
                    }
                }
                Platform.VELOCITY -> {
                    if (eventBind != null) {
                        if (method.parameterTypes[0] == OptionalEvent::class.java) {
                            registerListener(eventBind, event.postOrder) { method.invoke(instance, OptionalEvent(it)) }
                        } else {
                            error("unsupported parameter for optional event")
                        }
                    } else {
                        registerListener(method.parameterTypes[0], event.postOrder) { method.invoke(instance, it) }
                    }
                }
                else -> {
                    if (eventBind != null) {
                        if (method.parameterTypes[0] == OptionalEvent::class.java) {
                            registerListener(eventBind, event.priority, event.ignoreCancelled) { method.invoke(instance, OptionalEvent(it)) }
                        } else {
                            error("unsupported parameter for optional event")
                        }
                    } else {
                        registerListener(method.parameterTypes[0], event.priority, event.ignoreCancelled) { method.invoke(instance, it) }
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