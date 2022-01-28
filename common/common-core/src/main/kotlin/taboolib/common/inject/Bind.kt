package taboolib.common.inject

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Bind(val value: Array<KClass<out Annotation>>, val type: Array<KClass<*>> = [], val target: Target = Target.ALL) {

    enum class Target {

        CLASS, FIELD, METHOD, ALL
    }
}