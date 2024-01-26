package taboolib.common.platform

/**
 * 1. 忽略注入
 * 2. 忽略事件加载警告
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Ghost