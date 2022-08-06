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

@JvmOverloads
fun submit(
    now: Boolean = false,
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    commit: String? = null,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    return PlatformFactory.getService<PlatformExecutor>().submit(PlatformExecutor.PlatformRunnable(now, async, delay, period, commit, executor))
}

@JvmOverloads
fun submitAsync(
    now: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    commit: String? = null,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    return PlatformFactory.getService<PlatformExecutor>().submit(PlatformExecutor.PlatformRunnable(now, true, delay, period, commit, executor))
}