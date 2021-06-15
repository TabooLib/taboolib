package taboolib.platform

import net.md_5.bungee.api.scheduler.ScheduledTask
import taboolib.common.platform.PlatformAPI
import taboolib.common.platform.PlatformExecutor

@PlatformAPI
class BungeeExecutor : PlatformExecutor {

    private val plugin = BungeePlugin.instance

    override fun execute(
        async: Boolean,
        delay: Long,
        period: Long,
        executor: PlatformExecutor.PlatformTask.() -> Unit
    ): PlatformExecutor.PlatformTask {
        val task: BungeePlatformTask
        when {
            period > 0 -> if (async) {

            }
        }
    }

    class BungeePlatformTask(val scheduledTask: ScheduledTask) : PlatformExecutor.PlatformTask {

        override fun cancel() {
            scheduledTask.cancel()
        }
    }
}