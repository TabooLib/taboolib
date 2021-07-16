@file:Isolated

package taboolib.common.util

import taboolib.common.Isolated
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import java.io.Closeable
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

fun String.replaceWithOrder(vararg args: Any): String {
    if (args.isEmpty() || isEmpty()) {
        return this
    }
    val chars = toCharArray()
    val builder = StringBuilder(length)
    var i = 0
    while (i < chars.size) {
        val mark = i
        if (chars[i] == '{') {
            var num = 0
            while (i + 1 < chars.size && Character.isDigit(chars[i + 1])) {
                i++
                num *= 10
                num += chars[i] - '0'
            }
            if (i != mark && i + 1 < chars.size && chars[i + 1] == '}') {
                i++
                builder.append(args[num])
            } else {
                i = mark
            }
        }
        if (mark == i) {
            builder.append(chars[i])
        }
        i++
    }
    return builder.toString()
}

fun join(args: Array<String>, start: Int = 0, separator: String = " "): String {
    return args.filterIndexed { index, _ -> index >= start }.joinToString(separator)
}

fun <T> subList(list: List<T>, start: Int = 0, end: Int = list.size): List<T> {
    return list.filterIndexed { index, _ -> index in start until end }
}

fun <K, V> subMap(map: Map<K, V>, start: Int = 0, end: Int = map.size - 1): List<Map.Entry<K, V>> {
    return map.entries.filterIndexed { index, _ -> index in start..end }
}

fun <T> MutableList<T>.setSafely(index: Int, element: T, def: T) {
    while (size <= index) {
        add(def)
    }
    this[index] = element
}

fun <T> MutableList<T>.addSafely(index: Int, element: T, def: T) {
    while (size <= index) {
        add(def)
    }
    add(index, element)
}

fun Any.asList(): List<String> {
    return if (this is Collection<*>) map { it.toString() } else listOf(toString())
}

fun random(): Random {
    return ThreadLocalRandom.current()
}

fun random(v: Double): Boolean {
    return ThreadLocalRandom.current().nextDouble() <= v
}

fun random(v: Int): Int {
    return ThreadLocalRandom.current().nextInt(v)
}

fun random(num1: Int, num2: Int): Int {
    val min = min(num1, num2)
    val max = max(num1, num2)
    return ThreadLocalRandom.current().nextInt(min, max + 1)
}

fun random(num1: Double, num2: Double): Double {
    val min = min(num1, num2)
    val max = max(num1, num2)
    return if (min == max) max else ThreadLocalRandom.current().nextDouble(min, max)
}

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

fun <T> Optional<T>.presentRun(func: T.() -> Unit) {
    ifPresent(func)
}

fun <T> Optional<T>.orNull(): T? {
    return orElse(null)
}

fun ProxyCommandSender?.isConsole(): Boolean {
    return this !is ProxyPlayer
}