package taboolib.common.platform

import taboolib.common.LifeCycle

/**
 * 自唤醒
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class Awake(val value: LifeCycle = LifeCycle.CONST)