package taboolib.common.platform

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(
    val monitor: Int = -1,
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false,
)