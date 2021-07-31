package taboolib.common.platform

import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import java.lang.reflect.Method
import java.util.function.Supplier

@Awake
object EventBus : Injector.Methods {

    override fun inject(method: Method, clazz: Class<*>, instance: Supplier<*>) {
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
            val obj = instance.get()
            when (runningPlatform) {
                Platform.BUKKIT, Platform.NUKKIT -> {
                    if (method.parameterTypes[0] == OptionalEvent::class.java) {
                        if (eventBind != null) {
                            registerListener(eventBind, event.priority, event.ignoreCancelled) { method.invoke(obj, OptionalEvent(it)) }
                        }
                    } else {
                        registerListener(method.parameterTypes[0], event.priority, event.ignoreCancelled) { method.invoke(obj, it) }
                    }
                }
                Platform.BUNGEE -> {
                    val level = if (event.level != 0) event.level else event.priority.level
                    if (method.parameterTypes[0] == OptionalEvent::class.java) {
                        if (eventBind != null) {
                            registerListener(eventBind, level, event.ignoreCancelled) { method.invoke(obj, OptionalEvent(it)) }
                        }
                    } else {
                        registerListener(method.parameterTypes[0], level, event.ignoreCancelled) { method.invoke(obj, it) }
                    }
                }
                Platform.VELOCITY -> {
                    if (method.parameterTypes[0] == OptionalEvent::class.java) {
                        if (eventBind != null) {
                            registerListener(eventBind, event.postOrder) { method.invoke(obj, OptionalEvent(it)) }
                        }
                    } else {
                        registerListener(method.parameterTypes[0], event.postOrder) { method.invoke(obj, it) }
                    }
                }
                Platform.SPONGE_API_7, Platform.SPONGE_API_8 -> {
                    if (method.parameterTypes[0] == OptionalEvent::class.java) {
                        if (eventBind != null) {
                            registerListener(eventBind, event.order, event.beforeModifications) { method.invoke(obj, OptionalEvent(it)) }
                        }
                    } else {
                        registerListener(method.parameterTypes[0], event.order, event.beforeModifications) { method.invoke(obj, it) }
                    }
                }
                else -> {
                }
            }
        }
    }

    override val priority: Byte
        get() = 0

    override val lifeCycle: LifeCycle
        get() = LifeCycle.ENABLE
}