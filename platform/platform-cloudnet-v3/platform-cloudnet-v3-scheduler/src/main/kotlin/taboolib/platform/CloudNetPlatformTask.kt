package taboolib.platform

import taboolib.common.platform.service.PlatformTask
import java.util.concurrent.CompletableFuture

/**
 * @author Leosouthey
 * @since 2022/2/6-2:37
 **/
class CloudNetPlatformTask(private val future: CompletableFuture<Unit>) : PlatformTask {

    override fun cancel() {
        future.complete(null)
    }
}