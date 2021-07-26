package taboolib.module.kether

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class KetherProperty(
    val bind: KClass<*>,
    val shared: Boolean = false,
)