package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformExecutor

fun submit(
    now: Boolean = false,
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    commit: String? = null,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    return PlatformFactory.getPlatformService<PlatformExecutor>().submit(PlatformExecutor.PlatformRunnable(now, async, delay, period, commit, executor))
}