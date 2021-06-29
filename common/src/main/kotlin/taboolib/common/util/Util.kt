@file:Isolated
package taboolib.common.util

import taboolib.common.io.Isolated
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

fun String.replaceWithOrder(vararg args: Any) = Strings.replaceWithOrder(this, *args)!!

fun join(args: Array<String>, start: Int = 0, separator: String = " "): String {
    return args.filterIndexed { index, _ -> index >= start }.joinToString(separator)
}

fun <T> subList(list: List<T>, start: Int = 0, end: Int = list.size - 1): List<T> {
    return list.filterIndexed { index, _ -> index in start..end }
}

fun <K, V> subMap(map: Map<K, V>, start: Int = 0, end: Int = map.size - 1): List<Map.Entry<K, V>> {
    return map.entries.filterIndexed { index, _ -> index in start..end }
}

fun MutableList<Any>.setSafely(index: Int, element: Any, def: Any = "") {
    while (size <= index) {
        add(def)
    }
    this[index] = element
}

fun MutableList<Any>.addSafely(index: Int, element: Any, def: Any = "") {
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