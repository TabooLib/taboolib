package taboolib.internal

import org.tabooproject.reflex.ClassAnnotation
import org.tabooproject.reflex.ClassMethod
import taboolib.common.TabooLib
import taboolib.common.io.InstGetter
import taboolib.common.platform.Platform
import taboolib.common.platform.event.*
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
        val bindEventClass = bind?.let { runCatching { Class.forName(it) } }?.getOrNull()

        if (isOptionalEvent && bindEventClass == null) {
            return
        }

        val instanceValue = instance.get() ?: return

        when (TabooLib.runningPlatform()) {
            Platform.BUKKIT, Platform.NUKKIT ->
                registerBukkit(bindEventClass ?: type, annotation, method, instanceValue, isOptionalEvent)

            Platform.BUNGEE ->
                registerBungee(bindEventClass ?: type, annotation, method, instanceValue, isOptionalEvent)

            Platform.VELOCITY ->
                registerVelocity(bindEventClass ?: type, annotation, method, instanceValue, isOptionalEvent)

            Platform.SPONGE_API_7, Platform.SPONGE_API_8 ->
                registerSponge(bindEventClass ?: type, annotation, method, instanceValue, isOptionalEvent)

            else -> {}
        }
    }

    fun registerBukkit(
        event: Class<*>,
        annotation: ClassAnnotation,
        method: ClassMethod,
        instance: Any,
        isOptionalEvent: Boolean
    ) {
        val priority = annotation.enum<EventPriority>("priority")
        val ignoreCancelled = annotation.property<Boolean>("ignoreCancelled")!!

        registerBukkitListener(event, priority, ignoreCancelled) {
            method.invoke(instance, if (isOptionalEvent) OptionalEvent(it) else it)
        }
    }

    fun registerBungee(
        event: Class<*>,
        annotation: ClassAnnotation,
        method: ClassMethod,
        instance: Any,
        isOptionalEvent: Boolean
    ) {
        val level = if (annotation.property<Int>("level") != null) annotation.property<Int>("level")!! else annotation.enum<EventPriority>("priority").level
        val ignoreCancelled = annotation.property<Boolean>("ignoreCancelled")!!

        registerBungeeListener(event, level, ignoreCancelled) {
            method.invoke(instance, if (isOptionalEvent) OptionalEvent(it) else it)
        }
    }

    fun registerVelocity(
        event: Class<*>,
        annotation: ClassAnnotation,
        method: ClassMethod,
        instance: Any,
        isOptionalEvent: Boolean
    ) {
        val order = annotation.enum<PostOrder>("postOrder")

        registerVelocityListener(event, order) {
            method.invoke(instance, if (isOptionalEvent) OptionalEvent(it) else it)
        }
    }

    fun registerSponge(
        event: Class<*>,
        annotation: ClassAnnotation,
        method: ClassMethod,
        instance: Any,
        isOptionalEvent: Boolean
    ) {
        val order = annotation.enum<EventOrder>("postOrder")
        val before = annotation.enum<Boolean>("beforeModifications")

        registerSpongeListener(event, order, before) {
            method.invoke(instance, if (isOptionalEvent) OptionalEvent(it) else it)
        }
    }
}
