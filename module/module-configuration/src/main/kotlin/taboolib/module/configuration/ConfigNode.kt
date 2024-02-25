package taboolib.module.configuration

@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigNode(val value: String = "", val bind: String = "config.yml")