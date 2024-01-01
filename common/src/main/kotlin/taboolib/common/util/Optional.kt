@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common.util

import taboolib.common.Isolated
import java.util.*

/**
 * 当 [Optional] 存在时执行
 *
 * @param func 执行函数
 */
inline fun <T> Optional<T>.presentRun(noinline func: T.() -> Unit) {
    ifPresent(func)
}

inline fun <T> Optional<T>.orNull(): T? {
    return orElse(null)
}

inline fun <T> optional(value: Any, func: () -> T): T? {
    try {
        return func()
    } catch (ex: NullPointerException) {
        IllegalStateException(value.toString(), ex).printStackTrace()
    }
    return null
}