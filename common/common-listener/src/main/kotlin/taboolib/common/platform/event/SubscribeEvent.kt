package taboolib.common.platform.event

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false,
    // only bungeecord platform
    val level: Int = -1,
    // only velocity
    val postOrder: PostOrder = PostOrder.NORMAL,
    // only sponge platform
    val order: EventOrder = EventOrder.DEFAULT,
    val beforeModifications: Boolean = false,
    // optional event
    val bind: String = ""
)