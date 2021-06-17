package taboolib.common5.util

import taboolib.common.util.Strings
import java.lang.Exception
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.script.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

val scriptEngineManager = ScriptEngineManager()

val scriptEngineFactory by lazy {
    scriptEngineManager.engineFactories.firstOrNull { it.engineName.contains("Nashorn") }
}

val scriptEngine by lazy {
    scriptEngineFactory?.scriptEngine
}

fun String.compileJS() = (scriptEngine as? Compilable)?.compile(this)

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
    this.add(index, element)
}

fun String.toPrinted(separator: String = ""): List<String> {
    val result = ArrayList<String>()
    var i = 0
    while (i < length) {
        if (get(i) == '§') {
            i++
        } else {
            result.add("${substring(0, i + 1)}${if (i % 2 == 1) separator else ""}")
        }
        i++
    }
    if (separator.isNotEmpty() && i % 2 == 0) {
        result.add(this)
    }
    return result
}

fun Any.asList(): List<String> {
    return if (this is Collection<*>) map { it.toString() } else listOf(toString())
}

inline fun <T> Iterable<T>.subBy(selector: (T) -> Int): Int {
    var sum = selector(firstOrNull() ?: return 0)
    forEachIndexed { index, element ->
        if (index > 0) {
            sum -= selector(element)
        }
    }
    return sum
}

inline fun <T> Iterable<T>.subByDouble(selector: (T) -> Double): Double {
    var sum = selector(firstOrNull() ?: return 0.0)
    forEachIndexed { index, element ->
        if (index > 0) {
            sum -= selector(element)
        }
    }
    return sum
}

inline fun <T> Iterable<T>.mulBy(selector: (T) -> Int): Int {
    var sum = 1
    for (element in this) {
        sum *= selector(element)
    }
    return sum
}

inline fun <T> Iterable<T>.mulByDouble(selector: (T) -> Double): Double {
    var sum = 1.0
    for (element in this) {
        sum *= selector(element)
    }
    return sum
}

inline fun <T> Iterable<T>.divBy(selector: (T) -> Int): Int {
    var sum = selector(firstOrNull() ?: return 0)
    forEachIndexed { index, element ->
        if (index > 0) {
            sum /= selector(element)
        }
    }
    return sum
}

inline fun <T> Iterable<T>.divByDouble(selector: (T) -> Double): Double {
    var sum = selector(firstOrNull() ?: return 0.0)
    forEachIndexed { index, element ->
        if (index > 0) {
            sum /= selector(element)
        }
    }
    return sum
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

fun Any.isInt(): Boolean {
    return try {
        Integer.parseInt(this.toString())
        true
    } catch (ex: Exception) {
        false
    }
}