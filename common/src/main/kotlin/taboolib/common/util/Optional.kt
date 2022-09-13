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

fun <T> optional(value: Any, func: () -> T): T? {
    try {
        return func()
    } catch (ex: NullPointerException) {
        IllegalStateException(value.toString(), ex).printStackTrace()
    }
    return null
}