package taboolib.common.platform

import taboolib.common.LifeCycle

/**
 * 自唤醒，在 CONST 下运行。此时插件还未实例化。
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class Awake(val value: LifeCycle = LifeCycle.CONST)