package taboolib.module.kether

/**
 * 是否全部为整数
 */
fun List<Any>.isAllInt(): Boolean {
    return all { it is Int || it.isInt() }
}

/**
 * 是否为整数
 */
fun Any.isInt(): Boolean {
    return try {
        Integer.parseInt(toString())
        true
    } catch (ex: Exception) {
        false
    }
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