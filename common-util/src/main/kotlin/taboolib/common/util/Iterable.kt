package taboolib.common.util

import java.io.Closeable

/**
 * 遍历一个集合
 * 可以在遍历的过程中使用 close() 方法结束遍历，以弥补 Kotlin 无法使用 break 的设计
 *
 * @param start 开始位置（可省略）
 * @param end 结束位置（可省略）
 * @param reversed 是否逆向
 * @param action 方法体
 * @return 最后一次方法体执行结果
 */
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

inline fun <K, V> Array<out K>.getFirst(get: (K) -> V?): V? {
    for (t in this) {
        val v = get(t)
        if (v != null) {
            return v
        }
    }
    return null
}