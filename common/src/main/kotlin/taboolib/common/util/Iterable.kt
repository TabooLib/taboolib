@file:Isolated
package taboolib.common.util

import taboolib.common.Isolated
import java.io.Closeable

fun <T, C : Iterable<T>, R> C.each(start: Int = -1, end: Int = -1, reversed: Boolean = false, action: Closeable.(index: Int, T) -> R?): R? {
    val trigger = object : Closeable {
        var closed = false
        override fun close() {
            closed = true
        }
    }
    if (reversed) {
        var index = toList().size
        for (item in reversed()) {
            if (index > end && (index <= start || start == -1)) {
                if (trigger.closed) {
                    break
                }
                val result = action(trigger, index, item)
                if (result != null && result != Unit) {
                    return result
                }
            }
            index--
        }
    } else {
        var index = 0
        for (item in this) {
            if (index >= start && (index < end || end == -1)) {
                if (trigger.closed) {
                    break
                }
                val result = action(trigger, index, item)
                if (result != null && result != Unit) {
                    return result
                }
            }
            index++
        }
    }
    return null
}