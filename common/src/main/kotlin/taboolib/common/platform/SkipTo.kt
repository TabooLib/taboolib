package taboolib.common.platform

import taboolib.common.LifeCycle

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SkipTo(val value: LifeCycle = LifeCycle.CONST)