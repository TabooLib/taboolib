package taboolib.platform

import taboolib.common.util.unsafeLazy

/**
 * TabooLib
 * taboolib.platform.Folia
 *
 * @author 坏黑
 * @since 2024/2/28 16:57
 */
object Folia {

    /** 是否运行在 Folia 服务端 */
    val isFolia by unsafeLazy {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler")
            return@unsafeLazy true
        } catch (_: Throwable) {
        }
        return@unsafeLazy false
    }
}