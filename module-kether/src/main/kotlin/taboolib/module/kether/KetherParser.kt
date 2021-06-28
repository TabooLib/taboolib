package taboolib.module.kether

@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class KetherParser(
    val value: Array<String> = [],
    val release: Array<String> = [],
    val namespace: String = "kether",
)