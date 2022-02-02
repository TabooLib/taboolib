package taboolib.internal

import org.tabooproject.reflex.ClassAnnotation
import org.tabooproject.reflex.ClassMethod
import taboolib.common.Internal
import taboolib.common.TabooLib
import taboolib.common.io.InstGetter
import taboolib.common.platform.Platform
import taboolib.common.platform.event.ProxyListenerRegister
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.OptionalEvent
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.registerBungeeListener
import taboolib.common.platform.function.registerSpongeListener
import taboolib.common.platform.function.registerVelocityListener

/**
 * @author 坏黑
 * @since 2022/1/31 6:33 PM
 */
@Internal
class SimpleListenerRegister : ProxyListenerRegister {

    override fun register(annotation: ClassAnnotation, method: ClassMethod, instance: InstGetter<*>) {
        val type = method.parameterTypes[0]
        val isOptionalEvent = type == OptionalEvent::class.java
        val bind = annotation.property<String>("bind")
        val bindEventClass = if (bind != null) kotlin.runCatching { Class.forName(bind) }.getOrNull() else null
        if (isOptionalEvent && bindEventClass == null) {
            return
        }
        when (TabooLib.runningPlatform()) {
            Platform.BUKKIT, Platform.NUKKIT -> {
                registerBukkit(bindEventClass ?: type, annotation, method, instance.get() ?: return, isOptionalEvent)
            }
            Platform.BUNGEE -> {
                registerBungee(bindEventClass ?: type, annotation, method, instance.get() ?: return, isOptionalEvent)
            }
            Platform.VELOCITY -> {
                registerVelocity(bindEventClass ?: type, annotation, method, instance.get() ?: return, isOptionalEvent)
            }
            Platform.SPONGE_API_7, Platform.SPONGE_API_8 -> {
                registerSponge(bindEventClass ?: type, annotation, method, instance.get() ?: return, isOptionalEvent)
            }
            else -> {
            }
        }
    }

    fun registerBukkit(event: Class<*>, annotation: ClassAnnotation, method: ClassMethod, instance: Any, isOptionalEvent: Boolean) {
        registerBukkitListener(event, annotation.enum("priority"), annotation.property("ignoreCancelled")!!) {
            method.invoke(instance, if (isOptionalEvent) OptionalEvent(it) else it)
        }
    }

    fun registerBungee(event: Class<*>, annotation: ClassAnnotation, method: ClassMethod, instance: Any, isOptionalEvent: Boolean) {
        val level = if (annotation.property<Int>("level") != null) annotation.property<Int>("level")!! else annotation.enum<EventPriority>("priority").level
        registerBungeeListener(event, level, annotation.property("ignoreCancelled")!!) {
            method.invoke(instance, if (isOptionalEvent) OptionalEvent(it) else it)
        }
    }

    fun registerVelocity(event: Class<*>, annotation: ClassAnnotation, method: ClassMethod, instance: Any, isOptionalEvent: Boolean) {
        registerVelocityListener(event, annotation.enum("postOrder")) {
            method.invoke(instance, if (isOptionalEvent) OptionalEvent(it) else it)
        }
    }

    fun registerSponge(event: Class<*>, annotation: ClassAnnotation, method: ClassMethod, instance: Any, isOptionalEvent: Boolean) {
        registerSpongeListener(event, annotation.enum("order"), annotation.property("beforeModifications")!!) {
            method.invoke(instance, if (isOptionalEvent) OptionalEvent(it) else it)
        }
    }
}