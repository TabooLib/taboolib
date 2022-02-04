package taboolib.module.configuration

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigNode(val value: String = "", val bind: String = "config.yml")