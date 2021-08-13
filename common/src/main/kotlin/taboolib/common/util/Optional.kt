@file:Isolated
package taboolib.common.util

import taboolib.common.Isolated
import java.util.*

fun <T> Optional<T>.presentRun(func: T.() -> Unit) {
    ifPresent(func)
}

fun <T> Optional<T>.orNull(): T? {
    return orElse(null)
}