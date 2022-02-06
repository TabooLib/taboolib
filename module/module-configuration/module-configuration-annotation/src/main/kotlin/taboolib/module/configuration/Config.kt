package taboolib.module.configuration

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Config(val value: String = "config.yml", val autoload: Boolean = false)