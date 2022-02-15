package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.platform.service.PlatformRunnable
import taboolib.common.platform.service.PlatformTask

fun submit(
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    commit: String? = null,
    executor: PlatformTask.() -> Unit
): PlatformTask {
    return PlatformFactory
        .getPlatformService<PlatformExecutor>()
        .submit(PlatformRunnable(async, delay, period, commit, executor))
}
