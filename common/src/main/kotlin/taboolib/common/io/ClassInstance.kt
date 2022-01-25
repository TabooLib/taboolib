package taboolib.common.io

import taboolib.common.InstGetter
import taboolib.common.platform.PlatformFactory
import taboolib.internal.ExceptionInstGetter
import taboolib.internal.LazyInstGetter

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
        return ExceptionInstGetter(this, ex)
    }
    return LazyInstGetter.of(this, newInstance)
}

/**
 * 根据当前运行平台获取给定类的实例
 */
@Suppress("UNCHECKED_CAST")
fun <T> Class<T>.findInstanceFromPlatform(): T? {
    return runningClasses.firstOrNull { isAssignableFrom(it) && it != this && PlatformFactory.checkPlatform(it) }?.findInstance(true) as T?
}