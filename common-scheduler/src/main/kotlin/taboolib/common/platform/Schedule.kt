package taboolib.common.platform

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Schedule(
    val async: Boolean = false,
    val delay: Long = 0,
    val period: Long = 0
)
