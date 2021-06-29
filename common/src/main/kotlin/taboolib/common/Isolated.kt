package taboolib.common

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@kotlin.annotation.Retention(AnnotationRetention.BINARY)
annotation class Isolated(val exclude: Array<KClass<*>> = [Unit::class])