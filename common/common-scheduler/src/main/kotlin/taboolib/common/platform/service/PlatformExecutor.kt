package taboolib.common.platform.service

import taboolib.common.platform.PlatformService

@PlatformService
interface PlatformExecutor {

    fun start()

    fun submit(runnable: PlatformRunnable): PlatformTask

    class PlatformRunnable(val now: Boolean, val async: Boolean, val delay: Long, val period: Long, val commit: String?, val executor: PlatformTask.() -> Unit)

    interface PlatformTask {

        fun cancel()
    }
}