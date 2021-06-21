package taboolib.common.platform

@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(
    val priority: EventPriority = EventPriority.NORMAL, val ignoreCancelled: Boolean = false,
    // only bungeecord platform
    val level: Int = -1,
    // only sponge platform
    val order: EventOrder = EventOrder.DEFAULT, val beforeModifications: Boolean = false,
)