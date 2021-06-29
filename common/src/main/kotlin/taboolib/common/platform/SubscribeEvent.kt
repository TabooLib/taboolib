package taboolib.common.platform

import taboolib.common.io.Isolated

@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Isolated
annotation class SubscribeEvent(
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false,
    // only bungeecord platform
    val level: Int = -1,
    // only sponge platform
    val order: EventOrder = EventOrder.DEFAULT,
    val beforeModifications: Boolean = false,
)