@file:Isolated
package taboolib.common.util

import taboolib.common.Isolated
import taboolib.common.platform.function.submit
import java.util.concurrent.CompletableFuture

fun <T> sync(func: () -> T): T {
    val future = CompletableFuture<T>()
    submit { future.complete(func()) }
    return future.get()
}
