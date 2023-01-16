package taboolib.expansion.ioc.annotation

@Retention(AnnotationRetention.RUNTIME)
annotation class Component(
    val function: String = "Gson",
)