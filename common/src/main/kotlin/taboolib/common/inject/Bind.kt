package taboolib.common.inject

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Bind(val value: Array<KClass<*>>, val type: Type, val annotation: KClass<out Annotation>) {

    enum class Type {

        CLASS, FIELD, METHOD
    }
}