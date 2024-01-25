package taboolib.common.platform

import taboolib.common.io.getInstance
import taboolib.common.io.runningClasses

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PlatformImplementation(val platform: Platform)

/**
 * 搜索该类的当前平台实现
 */
@Suppress("UNCHECKED_CAST")
fun <T> Class<T>.findImplementation(): T? {
    return runningClasses.firstOrNull { isAssignableFrom(it) && it != this && PlatformFactory.checkPlatform(it) }?.getInstance(true)?.get() as? T
}