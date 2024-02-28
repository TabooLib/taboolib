package taboolib.platform

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import io.papermc.paper.threadedregions.scheduler.RegionScheduler
import org.bukkit.Bukkit
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.util.unsafeLazy

/**
 * TabooLib
 * taboolib.platform.FoliaExecutor
 *
 * @author 坏黑
 * @since 2024/2/28 16:50
 */
object FoliaExecutor {

    val regionScheduler by unsafeLazy {
        Bukkit::class.java.invokeMethod<RegionScheduler>("getRegionScheduler", isStatic = true)!!
    }

    val asyncScheduler by unsafeLazy {
        Bukkit::class.java.invokeMethod<AsyncScheduler>("getAsyncScheduler", isStatic = true)!!
    }

    val globalRegionScheduler by unsafeLazy {
        Bukkit::class.java.invokeMethod<GlobalRegionScheduler>("getGlobalRegionScheduler", isStatic = true)!!
    }
}