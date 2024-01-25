@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common.platform.function

import taboolib.common.Isolated
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory

/**
 * 当前运行平台
 */
inline val runningPlatform: Platform
    get() = TabooLib.getRunningPlatform()

/**
 * 停用插件
 */
inline fun disablePlugin() {
    TabooLib.setStopped(true)
}

/**
 * 推迟任务到指定生命周期下执行，如果生命周期已经过去则立即执行
 *
 * @param lifeCycle 生命周期
 * @param runnable  任务
 */
inline fun postpone(lifeCycle: LifeCycle = LifeCycle.ENABLE, runnable: Runnable) {
    TabooLib.postpone(lifeCycle, runnable)
}

/**
 * 获取实现类
 */
inline fun <reified T> implementation(): T {
    return PlatformFactory.getAPI()
}

/**
 * 获取实现类（可能为空）
 */
inline fun <reified T> implementationOrNull(): T? {
    return PlatformFactory.getAPIOrNull()
}

/**
 * 获取实现类
 */
@Deprecated("use implementation<T>() instead", ReplaceWith("implementation<T>()"))
inline fun <reified T> implementations(): T {
    return PlatformFactory.getAPI()
}