package taboolib.platform

import taboolib.common.platform.service.PlatformTask
import taboolib.internal.Internal
import java.util.concurrent.CompletableFuture

/**
 * @author Leosouthey
 * @since 2022/2/6-1:48
 **/
@Internal
class BungeePlatformTask(private val future: CompletableFuture<Unit>) : PlatformTask {

    override fun cancel() {
        future.complete(null)
    }
}