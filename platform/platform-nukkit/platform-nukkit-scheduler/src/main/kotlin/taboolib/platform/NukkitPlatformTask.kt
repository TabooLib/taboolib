package taboolib.platform

import cn.nukkit.scheduler.NukkitRunnable
import taboolib.common.platform.service.PlatformTask
import taboolib.internal.Internal

/**
 * @author Leosouthey
 * @since 2022/2/5-22:13
 **/
@Internal
class NukkitPlatformTask(val runnable: NukkitRunnable) : PlatformTask {

    override fun cancel() {
        runnable.cancel()
    }
}