package taboolib.common.io

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@kotlin.annotation.Retention(AnnotationRetention.BINARY)
annotation class Isolated(val bind: Array<KClass<*>> = [Unit::class])