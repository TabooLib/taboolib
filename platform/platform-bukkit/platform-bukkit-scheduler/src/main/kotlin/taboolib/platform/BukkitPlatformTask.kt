package taboolib.platform

import org.bukkit.scheduler.BukkitRunnable
import taboolib.internal.Internal
import taboolib.common.platform.service.PlatformTask

/**
 * TabooLib
 * taboolib.platform.BukkitPlatformTask
 *
 * @author sky
 * @since 2021/6/15 11:17 下午
 */
@Internal
class BukkitPlatformTask(val runnable: BukkitRunnable) : PlatformTask {

    override fun cancel() {
        runnable.cancel()
    }
}