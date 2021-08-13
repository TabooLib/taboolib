@file:Isolated
package taboolib.common.util

import taboolib.common.Isolated

fun Any.asList(): List<String> {
    return if (this is Collection<*>) map { it.toString() } else listOf(toString())
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