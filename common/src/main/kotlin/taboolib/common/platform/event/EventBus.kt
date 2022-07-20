package taboolib.common.platform.event

import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.function.*
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
                            registerBukkitListener(eventBind, event.priority, event.ignoreCancelled) { method.invoke(obj, OptionalEvent(it)) }
                        }
                    } else {
                        registerBukkitListener(method.parameterTypes[0], event.priority, event.ignoreCancelled) { method.invoke(obj, it) }
                    }
                }
                Platform.BUNGEE -> {
                    val level = if (event.level != 0) event.level else event.priority.level
                    if (method.parameterTypes[0] == OptionalEvent::class.java) {
                        if (eventBind != null) {
                            registerBungeeListener(eventBind, level, event.ignoreCancelled) { method.invoke(obj, OptionalEvent(it)) }
                        }
                    } else {
                        registerBungeeListener(method.parameterTypes[0], level, event.ignoreCancelled) { method.invoke(obj, it) }
                    }
                }
                Platform.VELOCITY -> {
                    if (method.parameterTypes[0] == OptionalEvent::class.java) {
                        if (eventBind != null) {
                            registerVelocityListener(eventBind, event.postOrder) { method.invoke(obj, OptionalEvent(it)) }
                        }
                    } else {
                        registerVelocityListener(method.parameterTypes[0], event.postOrder) { method.invoke(obj, it) }
                    }
                }
                Platform.SPONGE_API_7, Platform.SPONGE_API_8 -> {
                    postpone {
                        if (method.parameterTypes[0] == OptionalEvent::class.java) {
                            if (eventBind != null) {
                                registerSpongeListener(eventBind, event.order, event.beforeModifications) { method.invoke(obj, OptionalEvent(it)) }
                            }
                        } else {
                            registerSpongeListener(method.parameterTypes[0], event.order, event.beforeModifications) { method.invoke(obj, it) }
                        }
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