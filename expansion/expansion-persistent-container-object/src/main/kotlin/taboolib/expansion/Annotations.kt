package taboolib.expansion

import kotlin.reflect.KCallable

@Retention(AnnotationRetention.RUNTIME)
annotation class Id

@Retention(AnnotationRetention.RUNTIME)
annotation class Key

@Retention(AnnotationRetention.RUNTIME)
annotation class UniqueKey

@Retention(AnnotationRetention.RUNTIME)
annotation class Length(val value: Int = 64)

@Retention(AnnotationRetention.RUNTIME)
annotation class Alias(val value: String)