package taboolib.common.platform.event

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false,
    // 仅限 BungeeCord 使用
    val level: Int = -1,
    // 仅限 Velocity 使用
    val postOrder: PostOrder = PostOrder.NORMAL,
    // 用于 OptionalEvent
    val bind: String = ""
)