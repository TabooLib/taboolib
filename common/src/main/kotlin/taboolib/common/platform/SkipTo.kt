package taboolib.common.platform

import taboolib.common.LifeCycle

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SkipTo(val value: LifeCycle = LifeCycle.CONST)