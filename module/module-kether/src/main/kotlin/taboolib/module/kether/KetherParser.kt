package taboolib.module.kether

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KetherParser(
    val value: Array<String>,
    val release: Array<String> = [],
    val namespace: String = "kether",
    val shared: Boolean = false,
)