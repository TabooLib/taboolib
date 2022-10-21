@file:Isolated
package taboolib.common.platform.function

import taboolib.common.Isolated
import taboolib.common.LifeCycle
import taboolib.common.TabooLibCommon
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory

/**
 * 当前运行平台
 */
inline val runningPlatform: Platform
    get() = TabooLibCommon.getRunningPlatform()

/**
 * 停用插件
 */
fun disablePlugin() {
    TabooLibCommon.setStopped(true)
}

/**
 * 推迟任务到指定生命周期下执行，如果生命周期已经过去则立即执行
 *
 * @param lifeCycle 生命周期
 * @param runnable  任务
 */
fun postpone(lifeCycle: LifeCycle = LifeCycle.ENABLE, runnable: Runnable) {
    TabooLibCommon.postpone(lifeCycle, runnable)
}

/**
 * 获取实现类
 */
inline fun <reified T> implementations(): T {
    return PlatformFactory.getAPI()
}