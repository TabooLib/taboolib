package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformExecutor

/**
 * 释放在预备阶段的调度器计划
 * 这个方法只能执行一次且必须执行
 */
fun startExecutor() {
    PlatformFactory.getService<PlatformExecutor>().start()
}

/**
 * 执行一个任务
 */
fun runTask(executor: Runnable): PlatformExecutor.PlatformTask {
    return PlatformFactory.getService<PlatformExecutor>().submit(PlatformExecutor.PlatformRunnable(now = false, async = false, delay = 0, period = 0) { executor.run() })
}

/**
 * 注册一个调度器
 *
 * @param now 是否立即执行
 * @param async 是否异步执行
 * @param delay 延迟执行时间
 * @param period 重复执行时间
 * @param executor 调度器具体行为
 */
@JvmOverloads
fun submit(
    now: Boolean = false,
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    return PlatformFactory.getService<PlatformExecutor>().submit(PlatformExecutor.PlatformRunnable(now, async, delay, period, executor))
}

/**
 * 注册一个异步执行的调度器
 *
 * @param now 是否立即执行
 * @param delay 延迟执行时间
 * @param period 重复执行时间
 * @param executor 调度器具体行为
 */
@JvmOverloads
fun submitAsync(
    now: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    return PlatformFactory.getService<PlatformExecutor>().submit(PlatformExecutor.PlatformRunnable(now, true, delay, period, executor))
}