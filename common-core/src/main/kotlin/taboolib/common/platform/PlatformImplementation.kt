package taboolib.common.platform

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PlatformImplementation(val platform: Platform)
