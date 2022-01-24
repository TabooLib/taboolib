@file:Suppress("UNCHECKED_CAST")

package taboolib.common.io

import taboolib.common.*
import taboolib.common.platform.PlatformFactory

/**
 * 取该类在当前项目中被加载的任何实例
 * 例如：@Awake 自唤醒类，或是 Kotlin Companion Object、Kotlin Object 对象
 * @param newInstance 若无任何已加载的实例，是否实例化
 */
fun <T> Class<T>.findInstance(newInstance: Boolean = false): InstGetter<T> {
    try {
        val awoken = PlatformFactory.awokenMap[name] as? T
        if (awoken != null) {
            return InstantInstGetter(this, awoken)
        }
    } catch (ex: Throwable) {
        return ErrorInstGetter(this, ex)
    }
    return LazyInstGetter.of(this, newInstance)
}

/**
 * 获取平台实现
 */
fun <T> Class<T>.findImplementation(): T? {
    return runningClasses.firstOrNull { isAssignableFrom(it) && it != this && PlatformFactory.checkPlatform(it) }?.findInstance(true)?.get() as? T
}