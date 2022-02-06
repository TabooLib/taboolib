package taboolib.platform

import taboolib.common.platform.service.PlatformTask
import java.util.concurrent.CompletableFuture

/**
 * @author Leosouthey
 * @since 2022/2/6-18:38
 **/
class Sponge8PlatformTask(private val future: CompletableFuture<Unit>) : PlatformTask {

    override fun cancel() {
        future.complete(null)
    }
}