package taboolib.common.platform.service

import taboolib.common.Inject
import taboolib.common.platform.PlatformService

@Inject
@PlatformService
interface PlatformExecutor {

    fun submit(runnable: PlatformRunnable): PlatformTask

    fun start()

    class PlatformRunnable(val now: Boolean, val async: Boolean, val delay: Long, val period: Long, val executor: PlatformTask.() -> Unit)

    interface PlatformTask {

        fun cancel()
    }
}