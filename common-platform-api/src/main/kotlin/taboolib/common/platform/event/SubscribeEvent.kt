package taboolib.common.platform.event

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false,
    // 仅限 BungeeCord 和 Hytale 使用
    val level: Int = -1,
    // 仅限 Velocity 使用
    val postOrder: PostOrder = PostOrder.NORMAL,
    // 仅限 Hytale 使用
    val eventType: HytaleEventType = HytaleEventType.NORMAL,
    // 仅限 Hytale 使用，事件 key（用于带 key 的事件）
    val eventKey: String = "",
    // 用于 OptionalEvent
    val bind: String = ""
)