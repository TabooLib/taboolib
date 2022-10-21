package taboolib.common.platform.service

import taboolib.common.platform.PlatformService

@PlatformService
interface PlatformExecutor {

    fun submit(runnable: PlatformRunnable): PlatformTask

    fun start()

    class PlatformRunnable(val now: Boolean, val async: Boolean, val delay: Long, val period: Long, val comment: String?, val executor: PlatformTask.() -> Unit)

    interface PlatformTask {

        fun cancel()
    }
}