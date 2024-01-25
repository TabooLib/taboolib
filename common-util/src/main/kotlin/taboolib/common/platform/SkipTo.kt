package taboolib.common.platform

import taboolib.common.LifeCycle

/**
 * 在特定生命周期之前不触发注入行为（跳过）
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class SkipTo(val value: LifeCycle = LifeCycle.CONST)