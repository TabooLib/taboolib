package taboolib.common.platform.event

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false,
    val level: Int = -1,
    val postOrder: PostOrder = PostOrder.NORMAL,
    val order: EventOrder = EventOrder.DEFAULT,
    val beforeModifications: Boolean = false,
    val bind: String = ""
)